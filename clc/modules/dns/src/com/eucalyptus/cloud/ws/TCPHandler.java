/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2009, Eucalyptus Systems, Inc.
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Author: Neil Soman neil@eucalyptus.com
 */

package com.eucalyptus.cloud.ws;

import com.eucalyptus.util.DNSProperties;
import org.apache.log4j.Logger;
import org.xbill.DNS.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class TCPHandler extends ConnectionHandler {
    private static Logger LOG = Logger.getLogger( TCPHandler.class );
    Socket socket;
    public TCPHandler(Socket s) {
        this.socket = s;
    }

    public void run() {
        try {
            int inputLength;
            DataInputStream inStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            inputLength = inStream.readUnsignedShort();
            if(inputLength > DNSProperties.MAX_MESSAGE_SIZE) {
                LOG.error("Maximum message size exceeded. Ignoring request.");
            }
            byte[] inBytes = new byte[inputLength];
            inStream.readFully(inBytes);
            Message query;
            byte [] response = null;
            try {
                query = new Message(inBytes);
                response = generateReply(query, inBytes, inBytes.length, socket);
                if (response == null)
                    return;
            }
            catch (IOException exception) {
                LOG.error(exception);
            }
            outStream.writeShort(response.length);
            outStream.write(response);
        } catch(IOException ex) {
            LOG.error(ex);
        }
    }


}