if [ $# -eq 1 ]; then
    java -Djavax.net.ssl.keyStore="server.keystore" -Djavax.net.ssl.keyStorePassword=sdis3fixe -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis3fixe -cp bin/ service.Init $1
else
    java -Djavax.net.ssl.keyStore=server.keystore -Djavax.net.ssl.keyStorePassword=sdis3fixe -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis3fixe -cp bin/ service.Init $1 $2
fi
