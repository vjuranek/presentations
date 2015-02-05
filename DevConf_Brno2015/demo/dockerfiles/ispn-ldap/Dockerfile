FROM centos
MAINTAINER vjuranek <vjuranek@redhat.com>

RUN yum -y install openldap openldap-clients openldap-servers
RUN sed -i s/dc=my-domain,dc=com/dc=infinispan,dc=org/ /etc/openldap/slapd.d/cn=config/olcDatabase\=\{2\}bdb.ldif
RUN sed -i '/olcRootDN: cn=Manager,dc=infinispan,dc=org/a olcRootPW: {SSHA}+c+1pwQwQbhkvgWAWWOIt5la9Vk6PX+S' /etc/openldap/slapd.d/cn=config/olcDatabase\=\{2\}bdb.ldif
RUN sed -i s/dc=my-domain,dc=com/dc=infinispan,dc=org/ /etc/openldap/slapd.d/cn=config/olcDatabase\=\{1\}monitor.ldif
RUN sed -i '$a olcAccess: {0}to attrs=userPassword by self write by dn.base="cn=Manager,dc=infinispan,dc=org" write by anonymous auth by * none' /etc/openldap/slapd.d/cn=config/olcDatabase\=\{2\}bdb.ldif
RUN sed -i '$a olcAccess: {1}to * by dn.base="cn=Manager,dc=infinispan,dc=org" write by self write by * read' /etc/openldap/slapd.d/cn=config/olcDatabase\=\{2\}bdb.ldif

RUN chkconfig slapd on

ADD ispn.ldif /tmp/ispn.ldif
RUN service slapd start; sleep 10; service slapd status && ldapadd -f /tmp/ispn.ldif -D cn=Manager,dc=infinispan,dc=org -w ispnPassword

EXPOSE 389
CMD ["/usr/sbin/slapd","-u","ldap","-d","1"]