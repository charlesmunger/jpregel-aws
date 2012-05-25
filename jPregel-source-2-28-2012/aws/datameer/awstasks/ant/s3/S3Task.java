/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aws.datameer.awstasks.ant.s3;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

import aws.datameer.awstasks.ant.AbstractAwsTask;
import aws.datameer.awstasks.ant.s3.model.CreateBucketCommand;
import aws.datameer.awstasks.ant.s3.model.DeleteBucketCommand;
import aws.datameer.awstasks.ant.s3.model.ListBucketsCommand;
import aws.datameer.awstasks.ant.s3.model.S3Command;

public class S3Task extends AbstractAwsTask {

    private List<S3Command> _s3Commands = new ArrayList<S3Command>();

    public S3Task() {
        // default constructor - needed by ant
    }

    @Override
    public void execute() throws BuildException {
        System.out.println("executing " + getClass().getSimpleName());
        try {
            AmazonS3Client s3Service = createS3Service();
            for (S3Command s3Command : _s3Commands) {
                System.out.println("executing " + s3Command);
                s3Command.execute(getProject(), s3Service);
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public void addCreateBucket(CreateBucketCommand command) {
        _s3Commands.add(command);
    }

    public void addDeleteBucket(DeleteBucketCommand command) {
        _s3Commands.add(command);
    }

    public void addListBuckets(ListBucketsCommand command) {
        _s3Commands.add(command);
    }

    public AmazonS3Client createS3Service() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(_accessKey, _accessSecret);
        return new AmazonS3Client(awsCredentials);
    }

}
