package us.yamb.rmb.impl.builders;

import java.io.IOException;
import java.util.Map;

import us.yamb.amb.Send;
import us.yamb.amb.spi.AsyncResultImpl;
import us.yamb.mb.callbacks.AsyncResult;
import us.yamb.mb.callbacks.AsyncResult.AsyncResultCallback;
import us.yamb.rmb.Location;
import us.yamb.rmb.Message;
import us.yamb.rmb.RMB;
import us.yamb.rmb.Request;
import us.yamb.rmb.Request.Reply;
import us.yamb.rmb.impl.RMBImpl;
import us.yamb.rmb.impl.RMBMessage;

import com.ericsson.research.trap.utils.ThreadPool;

public class RequestImpl extends SendImpl<Request> implements Request
{
    
    private RMB  rmb;
    private long timeout = 30000;
    
    public RequestImpl(RMBImpl rmb, Send aSend)
    {
        super(rmb, aSend);
        this.rmb = rmb;
    }
    
    @Override
    public AsyncResult<Reply> execute() throws IOException
    {
        RMB resp = rmb.create();
        AsyncResultImpl<Reply> rv = new AsyncResultImpl<Request.Reply>();
        resp.onmessage(msg -> rv.completed(new ResponseImpl(msg)));
        ThreadPool.executeAfter(() -> {
            
            resp.remove();
            
            if (rv.called)
                return;
            
            // What to do if we weren't called?
            rv.completed(new ResponseImpl(new InterruptedException("Timeout exceeded without response")));
            
        },
                                timeout);
        send(resp);
        return rv;
    }
    
    @Override
    public void execute(AsyncResultCallback<Reply> callback) throws IOException
    {
        execute().setCallback(callback);
    }
    
    @Override
    public Request timeout(long msec)
    {
        timeout = msec;
        return this;
    }
    
    @Override
    public void execute(AsyncResultCallback<Reply> callback, AsyncResultCallback<IOException> exceptionHandler)
    {
        try
        {
            execute(callback);
        }
        catch (IOException e)
        {
            exceptionHandler.completed(e);
        }
    }
    
}

class ResponseImpl implements Reply
{
    private Exception err = null;
    private Message   msg;
    
    public ResponseImpl(Exception err)
    {
        this.err = err;
        msg = new RMBMessage<Message>();
    }
    
    public ResponseImpl(Message msg)
    {
        this.msg = msg;
        
    }
    
    @Override
    public Exception error()
    {
        return err;
    }
    
    @Override
    public Location from()
    {
        return msg.from();
    }
    
    @Override
    public Location to()
    {
        return msg.to();
    }
    
    @Override
    public String method()
    {
        return msg.method();
    }
    
    @Override
    public String header(String name)
    {
        return msg.header(name);
    }
    
    @Override
    public byte[] bytes()
    {
        return msg.bytes();
    }
    
    @Override
    public String string()
    {
        return msg.string();
    }
    
    @Override
    public <T> T object(Class<T> baseClass)
    {
        return msg.object(baseClass);
    }
    
    @Override
    public boolean confirmed()
    {
        return msg.confirmed();
    }
    
    @Override
    public long id()
    {
        return msg.id();
    }
    
    public String toString()
    {
        return msg.toString();
    }
    
    @Override
    public int status()
    {
        return msg.status();
    }
    
    @Override
    public Map<String, String> headers()
    {
        return msg.headers();
    }
    
}
