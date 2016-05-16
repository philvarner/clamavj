package com.philvarner.clamavj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClamScan {

    private static Log log = LogFactory.getLog(ClamScan.class);

    public static final int CHUNK_SIZE = 2048;
    private static final byte[] INSTREAM = "zINSTREAM\0".getBytes();
    private static final byte[] PING = "zPING\0".getBytes();
    private static final byte[] STATS = "nSTATS\n".getBytes();
    // TODO: IDSESSION, END

    //    It is mandatory to prefix this command with n or z, and all commands inside IDSESSION must  be
    //    prefixed.
    //
    //    Start/end  a  clamd  session. Within a session multiple SCAN, INSTREAM, FILDES, VERSION, STATS
    //    commands can be sent on the same socket without opening new connections.  Replies  from  clamd
    //    will  be  in  the form '<id>: <response>' where <id> is the request number (in ascii, starting
    //    from 1) and <response> is the usual clamd reply.  The reply lines have same delimiter  as  the
    //    corresponding  command had.  Clamd will process the commands asynchronously, and reply as soon
    //    as it has finished processing.
    //
    //    Clamd requires clients to read all the replies it sent, before sending more commands  to  pre-vent prevent
    //    vent  send()  deadlocks. The recommended way to implement a client that uses IDSESSION is with
    //    non-blocking sockets, and  a  select()/poll()  loop:  whenever  send  would  block,  sleep  in
    //    select/poll  until either you can write more data, or read more replies.  Note that using non-blocking nonblocking
    //    blocking sockets without the select/poll loop and  alternating  recv()/send()  doesn't  comply
    //    with clamd's requirements.
    //
    //    If  clamd detects that a client has deadlocked,  it will close the connection. Note that clamd
    //    may close an IDSESSION connection too if you don't follow the protocol's requirements.

    private int timeout;
    private String host;
    private int port;

    public ClamScan() {
    }

    public ClamScan(String host, int port, int timeout) {
        setHost(host);
        setPort(port);
        setTimeout(timeout);
    }

    public String stats() {
        return cmd(STATS);
    }

    public boolean ping() {
        return "PONG\0".equals(cmd(PING));
    }

    public String cmd(byte[] cmd) {

        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(getHost(), getPort()));
        } catch (IOException e) {
            log.error("could not connect to clamd server", e);
            return null;
        }

        try {
            socket.setSoTimeout(getTimeout());
        } catch (SocketException e) {
            log.error("Could not set socket timeout to " + getTimeout() + "ms", e);
        }

        DataOutputStream dos = null;
        StringBuilder response = new StringBuilder();
        try {  // finally to close resources

            try {
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                log.error("could not open socket OutputStream", e);
                return null;
            }

            try {
                dos.write(cmd);
                dos.flush();
            } catch (IOException e) {
                log.debug("error writing " + new String(cmd) + " command", e);
                return null;
            }

            InputStream is;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                log.error("error getting InputStream from socket", e);
                return null;
            }

            int read = CHUNK_SIZE;
            byte[] buffer = new byte[CHUNK_SIZE];

            while (read == CHUNK_SIZE) {
                try {
                    read = is.read(buffer);
                } catch (IOException e) {
                    log.error("error reading result from socket", e);
                    break;
                }
                response.append(new String(buffer, 0, read));
            }

        } finally {
            if (dos != null) try {
                dos.close();
            } catch (IOException e) {
                log.debug("exception closing DOS", e);
            }
            try {
                socket.close();
            } catch (IOException e) {
                log.debug("exception closing socket", e);
            }
        }

        if (log.isDebugEnabled()) log.debug("Response: " + response.toString());

        return response.toString();
    }

    /**
     * The method to call if you already have the content to scan in-memory as a byte array.
     *
     * @param in the byte array to scan
     * @return the result of the scan
     * @throws IOException
     */
    public ScanResult scan(byte[] in) throws IOException {
        return scan(new ByteArrayInputStream(in));
    }

    /**
     * The preferred method to call. This streams the contents of the InputStream to clamd, so
     * the entire content is not loaded into memory at the same time.
     *
     * @param in the InputStream to read.  The stream is NOT closed by this method.
     * @return a ScanResult representing the server response
     */
    public ScanResult scan(InputStream in) {

        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(getHost(), getPort()));
        } catch (IOException e) {
            log.error("could not connect to clamd server", e);
            return new ScanResult(e);
        }

        try {
            socket.setSoTimeout(getTimeout());
        } catch (SocketException e) {
            log.error("Could not set socket timeout to " + getTimeout() + "ms", e);
        }

        DataOutputStream dos = null;
        String response = "";
        try {  // finally to close resources

            try {
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                log.error("could not open socket OutputStream", e);
                return new ScanResult(e);
            }

            try {
                dos.write(INSTREAM);
            } catch (IOException e) {
                log.debug("error writing INSTREAM command", e);
                return new ScanResult(e);
            }

            int read = CHUNK_SIZE;
            byte[] buffer = new byte[CHUNK_SIZE];
            while (read != -1) {
                try {
                    read = in.read(buffer);
                } catch (IOException e) {
                    log.debug("error reading from InputStream", e);
                    return new ScanResult(e);
                }

                if (read != -1) { // send buffer if previous read did not exhaust the stream
                    try {
                        dos.writeInt(read);
                        dos.write(buffer, 0, read);
                    } catch (IOException e) {
                        log.debug("error writing data to socket", e);
                        break;
                    }
                }
            }

            try {
                dos.writeInt(0);
                dos.flush();
            } catch (IOException e) {
                log.debug("error writing zero-length chunk to socket", e);
            }

            try {
                read = socket.getInputStream().read(buffer);
            } catch (IOException e) {
                log.debug("error reading result from socket", e);
                read = 0;
            }

            if (read != -1) response = new String(buffer, 0, read);

        } finally {
            if (dos != null) try {
                dos.close();
            } catch (IOException e) {
                log.debug("exception closing DOS", e);
            }
            try {
                socket.close();
            } catch (IOException e) {
                log.debug("exception closing socket", e);
            }
        }

        if (log.isDebugEnabled()) log.debug("Response: " + response);

        return new ScanResult(response.trim());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Socket timeout in milliseconds
     *
     * @param timeout socket timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
