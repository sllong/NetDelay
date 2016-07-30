#!/usr/bin/env python

import os
import sys
import socket
from thread import *
import time
import datetime
import logging
from logging import Formatter
from logging.handlers import TimedRotatingFileHandler

from daemon import runner

HOST = ''   
TCPPORT = 10072 
UDPPORT = 10072

def SetupLogger(options):
    FORMAT = "%(asctime)-15s %(levelname)-8s %(filename)-16s %(message)s"
    formatter = Formatter(fmt=FORMAT)
    logger = logging.getLogger()

    if options.get('debug',False):
        handler = logging.StreamHandler()
        logger.setLevel(logging.DEBUG)
    else:
        handler = TimedRotatingFileHandler('%s/netecho.log' % options.get('root_path'),when="d",interval=1,backupCount=7)
        logger.setLevel(logging.DEBUG)

    handler.setFormatter(formatter)
    logger.addHandler(handler)
    return handler

class EchoServer():
    def __init__(self):
        self.stdin_path = '/dev/null'
        self.stdout_path = '/dev/tty'
        self.stderr_path = '/dev/tty'
        self.pidfile_path =  '/tmp/netecho.pid'
        self.pidfile_timeout = 10
        self.addr_map = {}
        self.conn_list = []

    def run(self):
        ts = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        #Bind socket to local host and port
        try:
            ts.bind((HOST, TCPPORT))
        except socket.error as msg:
            logging.error('Bind TCP Socket failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1])
            sys.exit()

        # Start Udp echo service
        # start_new_thread(self.ServerUdpThread,())

        ts.listen(10)
        logging.debug('Socket now listening')
         
        # Start Tcp echo service
        while 1:
            #wait to accept a connection - blocking call
            conn, addr = ts.accept()
            logging.debug('Connected with ' + addr[0] + ':' + str(addr[1]))

            self.addr_map = {conn:addr}
            self.conn_list.append((conn, addr))
            start_new_thread(self.tcpWorker,(conn,addr,))
         
        ts.close()

    def tcpWorker(self, conn, addr):

        while True:
            data = conn.recv(1024)
            if not data:
               break

            msg = datetime.datetime.now().strftime('%H:%M:%S.%f') + " " + data
            logging.debug(msg+' '+addr[0]+':'+str(addr[1]))

            if data[:1] == 'D': #DATA
                for c,addr in self.conn_list:
                    logging.debug("prepare to send to " + addr[0] + str(addr[1]))
                    if c == conn:
                        continue

                    logging.debug("send to " + addr[0] + str(addr[1]))
                    c.sendall(msg)
            else:
                conn.sendall(msg)

            #time.sleep(60)

        conn.close()

    def ServerUdpThread(self):
        us = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            us.bind((HOST, UDPPORT))
        except socket.error as msg:
            logging.error('Bind UDP Socket failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1])
            sys.exit()

        while True:
            data,addr = us.recvfrom(1024)
            if data:
                us.sendto(data,addr)

        us.close()


options = {
    'root_path': os.path.dirname(os.path.abspath(__file__)),
    'debug': True,                      #debug mode switch
    'address': ('192.168.2.2',10005),   #lbs server
}

if __name__ == "__main__":
    handler = SetupLogger(options)
    app = EchoServer()
    app.run()

    #daemon_runner = runner.DaemonRunner(app)
    # This ensures that the logger file handle does not get closed during daemonization
    #daemon_runner.daemon_context.files_preserve = [handler.stream]
    #daemon_runner.do_action()
