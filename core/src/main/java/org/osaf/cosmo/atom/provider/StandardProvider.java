package org.osaf.cosmo.atom.provider;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.Resolver;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.MediaCollectionAdapter;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetBuilder;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.Transactional;
import org.apache.abdera.protocol.server.WorkspaceManager;
import org.apache.abdera.protocol.server.context.AbstractResponseContext;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.impl.AbstractProvider;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;
import org.apache.abdera.util.EntityTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.ServiceGenerator;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.springframework.dao.OptimisticLockingFailureException;

public class StandardProvider extends AbstractProvider {

    private WorkspaceManager workspaceManager;
    private Resolver<Target> resolver;
    private ServiceLocatorFactory serviceLocatorFactory;
    private GeneratorFactory generatorFactory;
   
    private static final Log log = LogFactory.getLog(StandardProvider.class);
    
    @Override
    // Have to override because the base class method doesn't support
    // DELETE or PUT to a collection target, which we use.
    public ResponseContext process(RequestContext request) {
        ResponseContext response = preconditions(request);
        if(response!=null)
            return response;
        
        // resolve target
        Target target = request.getTarget();
        
        // ensure target was resolved
        if (target == null || target.getType() == TargetType.TYPE_NOT_FOUND) {
            response = ProviderHelper.notfound(request);
            response.setContentLength(0);
            return response;
        }
        
        String method = request.getMethod();
        TargetType type = target.getType();
        
        // Handle service document GET
        if (type == TargetType.TYPE_SERVICE && method.equalsIgnoreCase("GET")) {
            return getServiceDocument(request);
        }
        
        WorkspaceManager wm = getWorkspaceManager(request);

        // Lookup CollectionAdapter to handle request
        CollectionAdapter adapter = wm.getCollectionAdapter(request);
        if (adapter == null) {
            return ProviderHelper.notfound(request);
        }

        Transactional transaction = adapter instanceof Transactional ? (Transactional) adapter
                : null;
       
        try {
            if (transaction != null)
                transaction.start(request);
            if (type == TargetType.TYPE_CATEGORIES) {
                if (method.equalsIgnoreCase("GET"))
                    response = adapter.getCategories(request);
            } else if (type == TargetType.TYPE_COLLECTION) {
                if (method.equalsIgnoreCase("GET"))
                    response = adapter.getFeed(request);
                else if (method.equalsIgnoreCase("POST")) {
                    response = ProviderHelper.isAtom(request) ? adapter
                            .postEntry(request)
                            : adapter instanceof MediaCollectionAdapter ? ((MediaCollectionAdapter) adapter)
                                    .postMedia(request)
                                    : ProviderHelper.notsupported(request);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    if (adapter instanceof ExtendedCollectionAdapter)
                        response = ((ExtendedCollectionAdapter) adapter)
                                .deleteCollection(request);
                } else if (method.equalsIgnoreCase("PUT")) {
                    if (adapter instanceof ExtendedCollectionAdapter)
                        response = ((ExtendedCollectionAdapter) adapter)
                                .putCollection(request);
                }

            } else if (type == TargetType.TYPE_ENTRY) {
                if (method.equalsIgnoreCase("GET"))
                    response = adapter.getEntry(request);
                else if (method.equalsIgnoreCase("PUT"))
                    response = adapter.putEntry(request);
                else if (method.equalsIgnoreCase("DELETE"))
                    response = adapter.deleteEntry(request);
                else if (method.equalsIgnoreCase("HEAD"))
                    response = adapter.headEntry(request);
                else if (method.equalsIgnoreCase("OPTIONS"))
                    response = adapter.optionsEntry(request);
            } else if (type == TargetType.TYPE_MEDIA) {
                if (adapter instanceof MediaCollectionAdapter) {
                    MediaCollectionAdapter mcadapter = (MediaCollectionAdapter) adapter;
                    if (method.equalsIgnoreCase("GET"))
                        response = mcadapter.getMedia(request);
                    else if (method.equalsIgnoreCase("PUT"))
                        response = mcadapter.putMedia(request);
                    else if (method.equalsIgnoreCase("DELETE"))
                        response = mcadapter.deleteMedia(request);
                    else if (method.equalsIgnoreCase("HEAD"))
                        response = mcadapter.headMedia(request);
                    else if (method.equalsIgnoreCase("OPTIONS"))
                        response = mcadapter.optionsMedia(request);
                } else {
                    response = ProviderHelper.notsupported(request);
                }
            } else if (type == TargetType.TYPE_NOT_FOUND) {
                response = ProviderHelper.notfound(request);
            }
            
            // Forward all other requests to extensionRequest()
            if (response == null)
                response = adapter.extensionRequest(request);
            
            return response;
        } catch (Throwable e) {
            // We need a way to differentiate exceptions that are "expected" so that the
            // logs don't get too polluted with errors.  For example, OptimisticLockingFailureException
            // is expected and should be handled by the retry logic that is one layer above.
            // Although not ideal, for now simply check for this type and log at a different level.
            if(e instanceof OptimisticLockingFailureException) {
                log.info("Unexpected processing error", e);
            } else {
                log.error("Unexpected processing error", e);
            }
            
            if (transaction != null)
                transaction.compensate(request, e);
            response = ProviderHelper.servererror(request, e);
            return response;
        } finally {
            if (transaction != null)
                transaction.end(request, response);
        }
              
    }
    
    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((ServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
    }

    @Override
    protected TargetBuilder getTargetBuilder(RequestContext request) {
        return null;
    }

    @Override
    protected Resolver<Target> getTargetResolver(RequestContext request) {
        return resolver;
    }

    @Override
    protected WorkspaceManager getWorkspaceManager(RequestContext request) {
        return workspaceManager;
    }

    public void setWorkspaceManager(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    public void setTargetResolver(Resolver<Target> resolver) {
        this.resolver = resolver;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }
    
    @Override
    public ResponseContext getServiceDocument(RequestContext request) {
        // TODO handle other types of services somehow
        UserTarget target = (UserTarget) request.getTarget();
        User user = target.getUser();
        if (log.isDebugEnabled())
            log.debug("getting service for user " + user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ServiceGenerator generator = createServiceGenerator(locator);
            Service service =
                generator.generateService(target.getUser());

            return createResponseContext(request, service.getDocument());
        } catch (GeneratorException e) {
            String reason = "Unknown service generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }
    
    protected ServiceGenerator createServiceGenerator(ServiceLocator locator) {
        return getGeneratorFactory().createServiceGenerator(locator);
    }
    
    public GeneratorFactory getGeneratorFactory() {
        return generatorFactory;
    }

    public void setGeneratorFactory(GeneratorFactory factory) {
        generatorFactory = factory;
    }
    
    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    public void setServiceLocatorFactory(ServiceLocatorFactory factory) {
        serviceLocatorFactory = factory;
    }
    
    /**
     * Extends the superclass method to implement conditional request
     * methods by honoring conditional method headers for
     * <code>AuditableTarget</code>s.
     */
    protected ResponseContext preconditions(RequestContext request) {
        
        if (! (request.getTarget() instanceof AuditableTarget))
            return null;

        AuditableTarget target = (AuditableTarget) request.getTarget();

        ResponseContext response = null;
        
        response = ifMatch(request.getIfMatch(), target, request);
        if(response != null)
            return response;
       
        response = ifNoneMatch(request.getIfNoneMatch(), target, request);
        if(response != null)
            return response;
       
        response = ifModifiedSince(request.getIfModifiedSince(), target, request);
        if(response != null)
            return response;
       
        response = ifUnmodifiedSince(request.getIfUnmodifiedSince(), target, request);
        if(response != null)
            return response;

        return null;
    }

    private ResponseContext ifMatch(EntityTag[] etags,
                            AuditableTarget target,
                            RequestContext request) {
        if (etags.length == 0)
            return null;

        if (EntityTag.matchesAny(target.getEntityTag(), etags))
            return null;
        
        ResponseContext rc = ProviderHelper.preconditionfailed(request, "If-Match disallows conditional request");
        rc.setEntityTag(target.getEntityTag());
        return rc;
    }

    private ResponseContext ifNoneMatch(EntityTag[] etags,
                                AuditableTarget target,
                                RequestContext request) {
        if (etags.length == 0)
            return null;

        if (! EntityTag.matchesAny(target.getEntityTag(), etags))
            return null;

        ResponseContext rc;
        
        if (deservesNotModified(request))
            rc = ProviderHelper.notmodified(request, "Not Modified");
        else
            rc = ProviderHelper.preconditionfailed(request, "If-None-Match disallows conditional request");
    
        rc.setEntityTag(target.getEntityTag());
        return rc;
    }

    private ResponseContext ifModifiedSince(Date date,
                                    AuditableTarget target,
                                    RequestContext request) {
        if (date == null)
            return null;
        if (target.getLastModified().after(date))
            return null;
        
        return ProviderHelper.notmodified(request, "Not Modified");
    }

    private ResponseContext ifUnmodifiedSince(Date date,
                                      AuditableTarget target,
                                      RequestContext request) {
        if (date == null)
            return null;
        if (target.getLastModified().before(date))
            return null;
        
        return ProviderHelper.preconditionfailed(request, "If-Unmodified-Since disallows conditional request");
    }

    private boolean deservesNotModified(RequestContext request) {
        return (request.getMethod().equals("GET") ||
                request.getMethod().equals("HEAD"));
    }
    
    protected AbstractResponseContext createResponseContext(
            RequestContext context, Document<Element> doc) {
        return createResponseContext(context, doc, -1, null);
    }

    protected AbstractResponseContext createResponseContext(
            RequestContext context, Document<Element> doc, int status,
            String reason) {
        AbstractResponseContext rc = new BaseResponseContext<Document<Element>>(
                doc);

        rc.setWriter(context.getAbdera().getWriterFactory().getWriter(
                "PrettyXML"));

        if (status > 0)
            rc.setStatus(status);
        if (reason != null)
            rc.setStatusText(reason);

        // Cosmo data is sufficiently dynamic that clients
        // should always revalidate with the server rather than caching.
        rc.setMaxAge(0);
        rc.setMustRevalidate(true);
        rc.setExpires(new java.util.Date());

        return rc;
    }

}
