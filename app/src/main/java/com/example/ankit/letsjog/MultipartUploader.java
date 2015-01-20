package com.example.ankit.letsjog;


import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by ankit on 1/19/15.
 */

@SuppressWarnings("deprecation")
public class MultipartUploader extends MultipartEntity{

    private final ProgressListener listener;

    public MultipartUploader(final ProgressListener listener) {
        super();
        this.listener = listener;
    }

    public MultipartUploader(final HttpMultipartMode mode,
                             final ProgressListener listener) {
        super(mode);
        this.listener = listener;
    }

    public MultipartUploader(HttpMultipartMode mode, final String boundary,
                             final Charset charset, final ProgressListener listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, this.listener));
    }

    public static interface ProgressListener {
        void transferred(long num);
    }

    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;

        public CountingOutputStream(final OutputStream out,
                                    final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }



}