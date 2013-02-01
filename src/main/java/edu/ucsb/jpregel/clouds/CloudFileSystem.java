package edu.ucsb.jpregel.clouds;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import edu.ucsb.jpregel.system.FileSystem;
import java.io.*;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.InputStreamMap;

public class CloudFileSystem extends FileSystem {

    private final BlobStoreContext context;
    private final InputStreamMap storage;

    @Inject
    public CloudFileSystem(
    @Named("jobDirectoryName") String jobDirectoryName, 
    @Named("storage") ApiMetadata apiMetadata, 
    @Named("sAccess") String access, 
    @Named("sModify") String modify) {
        super(jobDirectoryName);
        this.context = new ContextBuilder(apiMetadata).credentials(access, modify).build(BlobStoreContext.class);
        this.storage = context.createInputStreamMap(jobDirectoryName);
        new File("in/").mkdirs();
        new File("out/").mkdirs();
    }

    @Override
    public BufferedReader getFileInputStream() throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(storage.get("input")));
    }

    @Override
    public BufferedWriter getFileOutputStream() {
        try {
            return new S3Writer("output");
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    @Override
    public BufferedReader getWorkerInputFileInputStream(int workerNum) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(storage.get("in/" + workerNum)));
    }

    @Override
    public BufferedWriter getWorkerInputFileOutputStream(int WorkerNum) {
        try {
            return new S3Writer("in/" + WorkerNum);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    @Override
    public BufferedReader getWorkerOutputFileInputStream(int workerNum) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(storage.get("out/"+workerNum)));
    }

    @Override
    public BufferedWriter getWorkerOutputFileOutputStream(int WorkerNum) {
        try {
            return new S3Writer("out/" + WorkerNum);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    private class S3Writer extends BufferedWriter {
        private final String path;
        
        S3Writer(String path) throws FileNotFoundException {
            super(new OutputStreamWriter(new FileOutputStream(new File(path))));
            this.path = path;
        }

        @Override
        public void close() throws IOException {
            super.close();
            storage.putFile(path, new File(path));
        }
    }
}