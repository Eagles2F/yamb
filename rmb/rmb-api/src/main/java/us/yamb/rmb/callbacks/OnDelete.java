package us.yamb.rmb.callbacks;

import us.yamb.rmb.Message;
import us.yamb.rmb.RMB;

public interface OnDelete extends RMBCallbackInterface
{

    public void onpost(RMB rmb, Message message);
}
