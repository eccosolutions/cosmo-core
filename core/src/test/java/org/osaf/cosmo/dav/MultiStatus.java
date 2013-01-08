/*
 * Copyright 2005-2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.dav;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Bean that encapsulates information about a DAV multistatus
 * response.
 */
public class MultiStatus {
    private static final Log log = LogFactory.getLog(MultiStatus.class);
    private static final Namespace NS = Namespace.getNamespace("D", "DAV:");

    private HashSet<MultiStatusResponse> responses;
    private String responseDescription;

    /**
     */
    public MultiStatus() {
        responses = new HashSet<MultiStatusResponse>();
    }

    /**
     */
    public Set<MultiStatusResponse> getResponses() {
        return responses;
    }

    /**
     */
    public MultiStatusResponse findResponse(String href) {
        for (MultiStatusResponse msr : responses) {
            if (msr.getHref().equals(href))
                return msr;
        }
        return null;
    }

    /**
     */
    public String getResponseDescription() {
        return responseDescription;
    }

    /**
     */
    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    /**
     */
    public static MultiStatus createFromXml(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("null document");
        }

        Element mse = doc.getDocumentElement();
        if (! DomUtil.matches(mse, "multistatus", NS)) {
            throw new IllegalArgumentException("root element not DAV:multistatus");
        }

        MultiStatus ms = new MultiStatus();

        ElementIterator i = DomUtil.getChildren(mse, "response", NS);
        while (i.hasNext()) {
            ms.getResponses().
                add(MultiStatusResponse.createFromXml(i.nextElement()));
        }

        String msrd = DomUtil.getChildTextTrim(mse, "responsedescription", NS);
        ms.setResponseDescription(msrd);

        return ms;
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("responses", responses).
            append("responseDescription", responseDescription).
            toString();
    }

    /**
     */
    public static class MultiStatusResponse {
        private String href;
        private Status status;
        private HashSet<PropStat> propstats;
        private String responseDescription;

        /**
         */
        public MultiStatusResponse() {
            propstats = new HashSet<PropStat>();
        }

        /**
         */
        public String getHref() {
            return href;
        }

        /**
         */
        public void setHref(String href) {
            this.href = href;
        }

        /**
         */
        public Status getStatus() {
            return status;
        }

        /**
         */
        public void setStatus(Status status) {
            this.status = status;
        }

        /**
         */
        public Set<PropStat> getPropStats() {
            return propstats;
        }

        /**
         */
        public PropStat findPropStat(int code) {
            for (PropStat ps : propstats) {
                if (ps.getStatus().getCode() == code)
                    return ps;
            }
            return null;
        }

        /**
         */
        public String getResponseDescription() {
            return responseDescription;
        }

        /**
         */
        public void setResponseDescription(String responseDescription) {
            this.responseDescription = responseDescription;
        }

        /**
         */
        public static MultiStatusResponse createFromXml(Element e) {
            if (e == null) {
                throw new IllegalArgumentException("null DAV:response element");
            }

            MultiStatusResponse msr = new MultiStatusResponse();

            Element he = DomUtil.getChildElement(e, "href", NS);
            if (he == null) {
                throw new IllegalArgumentException("expected DAV:href child for DAV:response element");
            }
            msr.setHref(DomUtil.getTextTrim(he));

            String statusLine = DomUtil.getChildTextTrim(e, "status", NS);
            if (statusLine != null) {
                msr.setStatus(Status.createFromStatusLine(statusLine));
            }

            ElementIterator i = DomUtil.getChildren(e, "propstat", NS);
            while (i.hasNext()) {
                msr.getPropStats().add(PropStat.createFromXml(i.nextElement()));
            }

            String msrrd =
                DomUtil.getChildTextTrim(e, "responsedescription", NS);
            msr.setResponseDescription(msrrd);

            return msr;
        }

        /**
         */
        public String toString() {
            return new ToStringBuilder(this).
                append("href", href).
                append("status", status).
                append("propstats", propstats).
                append("responseDescription", responseDescription).
                toString();
        }
    }

    /**
     */
    public static class PropStat {
        private HashSet<Element> props;
        private Status status;
        private String responseDescription;

        /**
         */
        public PropStat() {
            props = new HashSet<Element>();
        }

        /**
         */
        public Set<Element> getProps() {
            return props;
        }

        /**
         */
        public Element findProp(String name,
                                Namespace ns) {
            for (Element prop : props) {
                if (DomUtil.matches(prop, name, ns))
                    return prop;
            }
            return null;
        }

        /**
         */
        public Status getStatus() {
            return status;
        }

        /**
         */
        public void setStatus(Status status) {
            this.status = status;
        }

        /**
         */
        public String getResponseDescription() {
            return responseDescription;
        }

        /**
         */
        public void setResponseDescription(String responseDescription) {
            this.responseDescription = responseDescription;
        }

        /**
         */
        public static PropStat createFromXml(Element e) {
            if (e == null) {
                throw new IllegalArgumentException("null DAV:propstat element");
            }

            PropStat ps = new PropStat();

            Element pe = DomUtil.getChildElement(e, "prop", NS);
            if (pe == null) {
                throw new IllegalArgumentException("expected DAV:prop child for DAV:propstat element");
            }

            ElementIterator i = DomUtil.getChildren(pe);
            while (i.hasNext()) {
                ps.getProps().add(i.nextElement());
            }

            String statusLine = DomUtil.getChildTextTrim(e, "status", NS);
            if (statusLine != null) {
                ps.setStatus(Status.createFromStatusLine(statusLine));
            }

            String psrd =
                DomUtil.getChildTextTrim(e, "responsedescription", NS);
            ps.setResponseDescription(psrd);

            return ps;
        }

        /**
         */
        public String toString() {
            return new ToStringBuilder(this).
                append("props", props).
                append("status", status).
                append("responseDescription", responseDescription).
                toString();
        }
    }

    /**
     */
    public static class Status {
        private String protocol;
        private int code;
        private String reasonPhrase;

        /**
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        /**
         */
        public int getCode() {
            return code;
        }

        /**
         */
        public void setCode(int code) {
            this.code = code;
        }

        /**
         */
        public String getReasonPhrase() {
            return reasonPhrase;
        }

        /**
         */
        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        /**
         */
        public static Status createFromStatusLine(String line) {
            if (line == null) {
                throw new IllegalArgumentException("null status line");
            }

            String[] chunks = line.trim().split("\\s", 3);
            if (chunks.length < 3) {
                throw new IllegalArgumentException("status line " + line + " does not contain proto/version, code, reason phrase");
            }

            Status status = new Status();

            status.setProtocol(chunks[0]);
            status.setCode(Integer.parseInt(chunks[1]));
            status.setReasonPhrase(chunks[2]);

            return status;
        }

        /**
         */
        public String toString() {
            return new ToStringBuilder(this).
                append("protocol", protocol).
                append("code", code).
                append("reasonPhrase", reasonPhrase).
                toString();
        }
    }
}
