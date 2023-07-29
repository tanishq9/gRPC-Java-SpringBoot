Some Notes -

For gRPC server:
- Add dependency for: grpc-server-spring-boot-starter
- In app properties, mention:
    - grpc.server.port=6565
- Annotate the class which is implementing the proto contract with @GrpcService

For gRPC client:
- Add dependency for: grpc-client-spring-boot-starter
- In app properties, mention:
    - grpc.server.<client-name>.address:<address>
    - grpc.server.<client-name>.negotiationType:<type>
    - This will automatically create a ManagedChannel.
- Annotate the stub field in a class with @GrpcClient

@Transactional annotation:
- @Transactional annotation makes sure that the methods will be executed inside a database transaction.
- A database transaction is any operation that is treated as a single unit of work that either completes fully or does not complete at all and leaves the storage system in a consistent state.
- More: https://www.codeburps.com/post/transaction-management-in-spring@Transactional annotation makes sure that the methods will be executed inside a database transaction.
