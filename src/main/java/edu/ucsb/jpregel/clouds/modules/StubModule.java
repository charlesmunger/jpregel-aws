/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import edu.ucsb.jpregel.clouds.CloudFileSystem;
import edu.ucsb.jpregel.system.FileSystem;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.TransientApiMetadata;
import org.jclouds.compute.stub.StubApiMetadata;

/**
 *
 * @author Charles
 */
public class StubModule extends AbstractModule {
    private final String sAccess;
    private final String sModify;

    StubModule(String sAccess, String sModify) {
        this.sAccess = sAccess;
        this.sModify = sModify;
    }
	@Override
	protected void configure() {
		bind(FileSystem.class).to(CloudFileSystem.class);
		bind(ApiMetadata.class).annotatedWith(Names.named("storage")).toInstance(new StubApiMetadata());
		bind(ApiMetadata.class).annotatedWith(Names.named("compute")).toInstance(new TransientApiMetadata());
                bindConstant().annotatedWith(Names.named("sAccess")).to(sAccess);
                bindConstant().annotatedWith(Names.named("sModify")).to(sModify);
                bindConstant().annotatedWith(Names.named("cUser")).to(sAccess);
                bindConstant().annotatedWith(Names.named("cPass")).to(sModify);
	}
}
