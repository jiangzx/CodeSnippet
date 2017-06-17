import os
import json
import StringIO
import re
from datetime import datetime
import time


def tellSipHost(str):
    val = ''
    start = str.find('<sip:')
    end = str.find('>')
    if start!=-1 and end!=-1:
        val = str[start + 5:end]
        idx = val.find(';')
        if idx!=-1:
            val = val[0:idx]
    return val 

def returnNone():
    return 1,2,3

if __name__ == '__main__':
    # to_1 = "To: <sip:1234570**24693508@vr01amvoa00-vip.webex.com:5060;transport=tcp;rduuid=7CF1589A230-B076F12EC41-35271F14309-2072DDB248C>"
    # to_2 = "To: <sip:62.109.208.217:5068>"

    # print tellSipHost(to_1)
    # print '------------'
    # print tellSipHost(to_2)
    a,b,c = returnNone()
    print a,b,c

