__author__ = 'Cody Giles'
__license__ = "Creative Commons Attribution-ShareAlike 3.0 Unported License"
__version__ = "1.0"
__maintainer__ = "Cody Giles"
__status__= "Production"

import subprocess
import smtplib
from email.mime.text import MIMEText
import datetime
import sys

def connect_type(word_list):
    """ This function takes a list of words, then, depending which key word,
    returns the corresponding internet connection thype as a string,
    e.g. 'eternet'.
    """
    if 'wlan0' in word_list or 'wlan1' in word_list:
        con_type = 'wifi'
    elif 'eth0' in word_list:
        con_type = 'ethernet'
    else:
        con_type = 'current'

    return con_type

to = 'jcprumachuk@mac.com'
gmail_user = 'jcp1016@gmail.com'
gmail_pw   = 'blahblah555'
smtpserver = smtplib.SMTP('smtp.gmail.com', 587)
smtpserver.ehlo()
smtpserver.starttls()
smtpserver.ehlo()
smtpserver.login(gmail_user, gmail_pw)
today = datetime.date.today()

arg = 'ip route list'
p = subprocess.Popen(arg, shell=True, stdout=subprocess.PIPE)
data = p.communicate()

ip_lines = data[0].splitlines()
split_line_a = ip_lines[1].split()
split_line_b = ip_lines[2].split()

ip_type_a = connect_type(split_line_a)
ip_type_b = connect_type(split_line_b)

ipaddr_a = split_line_a[split_line_a.index('src')+1]
ipaddr_b = split_line_b[split_line_b.index('src')+1]

my_ip_a = 'Your %s ip is %s' % (ip_type_a, ipaddr_a)
my_ip_b = 'Your %s ip is %s' % (ip_type_b, ipaddr_b)

msg = MIMEText(my_ip_a + "\n\n" + my_ip_b)
msg['To'] = to
msg['Subject'] = 'Hello, Edison!'
smtpserver.sendmail(gmail_user, [to], msg.as_string())
smtpserver.quit()
