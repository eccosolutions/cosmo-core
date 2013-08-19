package org.osaf.cosmo.hibernate;

import org.springframework.orm.hibernate3.support.AbstractLobType;

public class BlobByteArrayType extends org.springframework.orm.hibernate3.support.BlobByteArrayType {
    public BlobByteArrayType() {
        super(CosmoLobHandler.INSTANCE, null);
    }
}
