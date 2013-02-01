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
import org.jclouds.ec2.EC2ApiMetadata;
import org.jclouds.s3.S3ApiMetadata;

/**
 *
 * @author Charles
 */
public class AWSModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FileSystem.class).to(CloudFileSystem.class);
		bind(ApiMetadata.class).annotatedWith(Names.named("storage")).toInstance(new S3ApiMetadata());
		bind(ApiMetadata.class).annotatedWith(Names.named("compute")).toInstance(new EC2ApiMetadata());
	}
}
