/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds.modules;

import api.ReservationService;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import edu.ucsb.jpregel.clouds.CloudReservationService;
import java.io.Serializable;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.s3.S3ApiMetadata;

/**
 *
 * @author Charles
 */
public class AWSModule extends AbstractModule implements Serializable {
    private final String sAccess;
    private final String sModify;

    public AWSModule(String sAccess, String sModify) {
        this.sAccess = sAccess;
        this.sModify = sModify;
    }
	@Override
	protected void configure() {
		bind(ApiMetadata.class).annotatedWith(Names.named("storage")).toInstance(new S3ApiMetadata());
		bindConstant().annotatedWith(Names.named("compute")).to("aws-ec2");
                bind(ReservationService.class).to(CloudReservationService.class);
                bindConstant().annotatedWith(Names.named("sAccess")).to(sAccess);
                bindConstant().annotatedWith(Names.named("sModify")).to(sModify);
                bindConstant().annotatedWith(Names.named("cUser")).to(sAccess);
                bindConstant().annotatedWith(Names.named("cPass")).to(sModify);
	}
}
