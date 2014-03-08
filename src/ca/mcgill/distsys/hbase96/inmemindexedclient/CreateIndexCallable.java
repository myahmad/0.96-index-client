package ca.mcgill.distsys.hbase96.inmemindexedclient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;

import com.google.protobuf.ByteString;


import ca.mcgill.distsys.hbase96.indexcommonsinmem.Util;
import ca.mcgill.distsys.hbase96.indexcoprocessorsinmem.protobuf.generated.IndexCoprocessorInMem.IndexCoprocessorCreateRequest;
import ca.mcgill.distsys.hbase96.indexcoprocessorsinmem.protobuf.generated.IndexCoprocessorInMem.IndexCoprocessorCreateResponse;
import ca.mcgill.distsys.hbase96.indexcoprocessorsinmem.protobuf.generated.IndexCoprocessorInMem.IndexCoprocessorInMemService;

public class CreateIndexCallable implements Batch.Call<IndexCoprocessorInMemService, Boolean> {

    private IndexCoprocessorCreateRequest request;

    
    // Modified by Cong
    public CreateIndexCallable(byte[] family, byte[] qualifier, String indexType, Object[] arguments) {
      IndexCoprocessorCreateRequest.Builder builder = IndexCoprocessorCreateRequest.newBuilder();
      builder.setFamily(ByteString.copyFrom(family));
      builder.setQualifier(ByteString.copyFrom(qualifier));
      builder.setIndexType(indexType);
      
      List<ByteString> bytesArguments = new ArrayList<ByteString>();
      for(int i = 0; i < arguments.length; i++){
    	  try {
			bytesArguments.add(ByteString.copyFrom(Util.serialize(arguments[i])));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      
      builder.addAllArguments(bytesArguments);
      
      
      request = builder.build();
    }

    public Boolean call(IndexCoprocessorInMemService instance) throws IOException {
        ServerRpcController controller = new ServerRpcController();
        BlockingRpcCallback<IndexCoprocessorCreateResponse> rpcCallback = new BlockingRpcCallback<IndexCoprocessorCreateResponse>();
        instance.createIndex(controller, request, rpcCallback);

        IndexCoprocessorCreateResponse response = rpcCallback.get();
        if (controller.failedOnException()) {
          throw controller.getFailedOn();
        }
        
        return response.getSuccess();
    }

}