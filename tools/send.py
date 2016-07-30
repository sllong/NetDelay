#!/usr/bin/env python
import select
from socket import *
import time

HOST = '119.254.211.165'#'192.168.2.174'
PORT = 10072

BUFSIZE = 1024

ADDR = (HOST, PORT)

sock = socket(AF_INET, SOCK_STREAM)

sock.connect(ADDR)

rfds,wfds,efds = select.select([],[sock,],[],10)
if len(wfds) == 0:
    print "error"

while True:
    
    data = raw_input('>')
    if not data:
        break

    sock.sendall(data)
    try:
        data= sock.recv(BUFSIZE)
        if not data:
            break
        print data
    except:
        print 'Recv Error'

    time.sleep(0.01)
    
sock.close()
