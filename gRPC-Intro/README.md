## Problem Statement

Inter-services calls, problem with this approach -
- Most of the time, we choose JSON with HTTP 1.1 for inter-service communication, which is more of a typical request and response protocol.
- It takes 3 client exchanges b/w client and server to form a TCP connection, then we sent the request and get response from server.
- You need to wait for response to come back before sending another request via the same connection.
    - Or we can create another connection, which takes significant amount of time.

HTTP is a stateless protocol
- So every request carries info in the form of cookie, user-agent, etc in the request headers, these are plaintext, heavy and cannot be compressed.


Serialisation and Deserialisation
- For every call sent from one service to another, the object payload is first serialised as json and then at another service end, it is deserialised into object.
- The serialised form is the json string or textual form which takes time, CPU and memory to process.
- And this happens for every single call.

API contract
- Good orgs maintains OpenAPI documentation for contract b/w services but not a standard.

gRPC -
- RPC stands for remote procedure calls.
- It is a framework for inter microservice communication.

## HTTP/2 vs HTTP/1.1

HTTP 1.1
- It is an application protocol which works on top of TCP i.e. the client and server exchange 3 messages first to form a stable connection, then the client sends request over the same connection, until it gets response, it cannot send another request.

HTTP/2
- HTTP/2 enable multiplexing i.e. one single TCP connection is enough for sending multiple parallel requests.
- HTTP/2 is a binary protocol (in HTTP 1.1 info was sent as plaintext) and we have 2 frames - Header frame and Data frame, through which we are sending info as binary, so it relatively smaller in size.
- Header compression, in request 1, we had sent all details in header, we don't need to repeat everything in request 2.
- In HTTP/2, we have flow control, it is like back-pressure, the sender will not send too much info when the receiver cannot handle it (in case of streaming RPC).

**gRPC is a framework that uses HTTP/2 as protocol.**

## RPC Calls and Types

Client's call to the gRPC server can be sync or async.
- Sync - Blocking/waiting for the response.
- Async - Register a listener for call back.

RPC Types -
- Unary
- Server-streaming
- Client-streaming
- Bidirectional-streaming

Unary RPC -
- In Unary RPC, either trigger .onNext() and .onCompleted() methods on the StreamObserver  OR trigger .onError() on the StreamObserver
- blockingStub - Wait for the response to come.

Server Streaming RPC -
```
// server-side streaming: server sending multiple responses
rpc withdraw(WithdrawRequest) returns (stream Money);
```
- https://developers.google.com/maps-booking/reference/grpc-api-v2/status_codes

Note -
- CountDownLatch is a better alternative to sleeping threads when testing async methods, use that only wherever applicable.
- CountDown the Latch in .onComplete() and .onError methods.
```
// Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
countDownLatch.await();
```

## Channel and Load Balancing

- HTTP/2 provides multiplexing support which provides support to send multiple parallel HTTP requests over single connection.
- ManagedChannel - This channel is an object and represents the HTTP/2 connection b/w client and server.
- By "persistent" connection, it doesn't mean life-long connection, it means the connection would be reused for multiple requests.

```
// 8585 port is where nginx is listening for HTTP/2 connections
ManagedChannel managedChannel = ManagedChannelBuilder
.forAddress("localhost", 8585)
.usePlaintext()
.build();
```

default.conf
```
upstream bankservers {
server host.docker.internal:6565;
server host.docker.internal:7575;
}

server {
# http2 has to be mentioned for grpc servers
listen 8585 http2;
    location / {
       grpc_pass grpc://bankservers;
    }
}
```

- Killing one of the gRPC servers fronted by nginx LB doesn't lead to failures on client-side as nginx will forward requests to only healthy servers.

### Important Notes

- Whenever we make a new rpc call, nginx would be balancing the load with available instances.
- In case of client streaming, there would be one rpc request and one instance would handle all request items streamed over for this request.
- If grpc server handling the client streaming requests fails then rpc would fail as well. ONLY if its a new rpc then request would be balanced.
- This is ^server side load balancing done using nginx.

### SubChannels

- 1 channel represents one connection b/w client and server, this connection is persistent.
- A channel can have many sub-channels, each sub-channel represents a connection to the server. Each sub-channel would represent one connection.
- Channel chooses sub-channel in pick-first strategy by default, it could be round-robin fashion.

### Intent of Server-side or Client-side Load Balancing

- Nginx, used as proxy, would be doing load balancing to multiple gRPC servers.
- The need to do load-balancing is to ensure that out of multiple gRPC servers, its shouldn't be that only one of them is receiving all requests.
- This can be ensured via server side LB using nginx and via client side LB using sub-channels.

## Deadline

- Deadline - Timeout for an RPC to complete.
- Setting the deadline cancels the request at client-side, but the server wasn't aware of this and will be still processing the request.
- It could be a problem in some scenarios. Basically we are wasting server resources by doing unnecessary work.
- gRPC provides an object Context, this is a namespace for current RPC to store and retrieve info. This also provides info if the current RPC is still alive or not.

```
// check if rpc call was canceled by client
if (!Context.current().isCancelled()) {
 responseObserver.onNext(money);
 System.out.println("Delivered 10$");
} else {
 break;
}
```

## Interceptor

- We can intercept RPCs at both the client and server side to handle the cross-cutting concerns.
- For client-side, we can use ClientInterceptor interface.
- For server-side, we can use ServerInterceptor interface.
- Default ClientInterceptor incase request has to be passed as-is.
```
@Override
public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
 return channel.newCall(methodDescriptor, callOptions);
}
```

- Global Deadline at client-side using ClientInterceptor -
```
// method (which we are calling), callOptions (config related to gRPC call to be made), channel
public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
 // return channel.newCall(methodDescriptor, callOptions);
 
 // deadline is set already in the call then do not override it
 Deadline deadline = callOptions.getDeadline();
 
 if (Objects.isNull(deadline)) {
  callOptions = callOptions.withDeadline(Deadline.after(1, TimeUnit.SECONDS));
 }
 
 return channel.newCall(methodDescriptor, callOptions);
}
```

### Interceptor Key/Value Pairs

gRPC provides a set of key-value pairs -

- Call Options - To send information from stub to the interceptor.
  - For example, we can set .withDeadline() at stub level and then interceptor would see that and take apt decision whether to use that or not.
  - CallCredentials will be attached as CallOption i.e. with every client request since the Creds are user/request specific.
- Metadata - To send data from client to server. This represents the http headers.
- Context - Once the metadata is received at server layer/interceptor, we can extract info from the metadata and if this info has to be passed to server business layer then Context will be used.
  - Context is a namespace which can be used to store and retrieve info specific to a rpc call on server side.
  - Easily put just like Call Options is used to pass info from stub to client interceptor, similarly Context is used to pass info from server interceptor to server.
```
Context context = Context.current().withValue(
  ServerConstants.CTX_USER_ROLE,
  role
);
// To pass context info to the service layer
return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
```

- Miscellaneous
  - About ThreadLocal -
    - The TheadLocal construct allows us to store data that will be accessible only by a specific thread.
    - https://www.baeldung.com/java-threadlocal
    - https://www.youtube.com/watch?v=a_BoqsnVR2U&ab_channel=JakobJenkov

## Error Handling using Metadata

Error Handling can be done in 2 ways:

- Error channel
  - Status code
  - Metadata
    - Along with status code, more information can be added for the error such as Metadata which can be an object of Error proto.

- Data channel
  - The proto schema could also be modified to return a custom Error proto object which would be populated incase of error. The response would be oneof this error proto or some success proto.

## SSL / TLS

- When client sends request to you then as part of TLS handshake, they verify if the server really has the certificate, this verification is done by the browser/OS.
- Not all APIs/endpoints are public, could be internal as well, incase that case they can have their own signed certificate. This is what we will simulate.
- Using OpenSSL, we can generate these certs.

### Steps

- Becoming CA

Created a private key - ca.key.pem
```
openssl genrsa -des3 -out ca.key.pem 2048
```

Created a public cert (public key) which will be used to sign the server cert, this would be created using the private key created in above step - ca.cert.pem
```
openssl req -x509 -new -nodes -key ca.key.pem -sha256 -days 365 -out ca.cert.pem
```

- Generating Certificate at Server side

Server will request CA to issue a certificate. To send a request, the server needs to have its own private key - localhost.key
```
openssl genrsa -out localhost.key 2048
```

Now, we need to generate a CSR (certificate signing request) at our end - localhost.csr
```
openssl req -new -key localhost.key -out localhost.csr
```

Send request to CA to sign the certificate - localhost.crt
```
openssl x509 -req -in localhost.csr -CA ca.cert.pem -CAkey ca.key.pem -CAcreateserial -out localhost.crt -days 365
```

- The .key file should be in .pem format as gRPC can only understand that format.
```
openssl pkcs8 -topk8 -nocrypt -in localhost.key -out localhost.pem
```

- Server and Client Side Code Changes

Server Side
```
SslContext sslContext = GrpcSslContexts.forServer(
        new File("/Users/tsaluja/Documents/CA_Test/localhost.crt"),
        new File("/Users/tsaluja/Documents/CA_Test/localhost.pem")
).build();

Server server = NettyServerBuilder.forPort(6565)
        .sslContext(sslContext)
        .addService(new BankService())
        .build();
```

Client Side
```
SslContext sslContext = GrpcSslContexts.forClient()
				.trustManager(new File("/Users/tsaluja/Documents/CA_Test/ca.cert.pem"))
				.build();

ManagedChannel managedChannel = NettyChannelBuilder
        .forAddress("localhost", 6565)
        .sslContext(sslContext)
        .build();
```
