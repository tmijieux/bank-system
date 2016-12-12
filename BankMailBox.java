import java.util.HashMap;
import java.util.Map;

import BankSystem.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;
import org.omg.CosEventChannelAdmin.AlreadyConnected;

public class BankMailBox
    extends PushConsumerPOA
{
    private BankServant m_bank;
    private ProxyPushSupplier m_supp;

    public BankMailBox(BankServant bank, ORB orb, EventChannel chan)
    {
        m_bank = bank;
        m_supp = NotificationServiceHelper.getPPushSupplier(chan);

        try {
            m_supp.connect_any_push_consumer( _this( orb ) );

        } catch (AlreadyConnected ac) {
            System.err.println("already connected");
            // fine ?? ... dont really know

        } catch (org.omg.CORBA.UserException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void offer_change(EventType et[], EventType et2[])
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void disconnect_push_consumer()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void push(Any any_event) // PushConsumer
    {
        System.out.println("[Bank"+m_bank.get_id()+"]  received 1 event (push)");
        // Receive event here;
        StructuredEvent ev;
        ev = StructuredEventHelper.extract(any_event);

        Map<String,String> prop = new HashMap<>();
        for (int i = 0; i < ev.filterable_data.length; ++i) {
            try {
                String key = ev.filterable_data[i].name;
                String val = ev.filterable_data[i].value.extract_string();
                prop.put(key, val);
            } catch (org.omg.CORBA.BAD_OPERATION ex) {
                ex.printStackTrace();
            }
        }

        Transaction T = new Transaction(
            Integer.parseInt(prop.get("id")),
            Integer.parseInt(prop.get("bank_id")),
            Integer.parseInt(prop.get("account_id")),
            Integer.parseInt(prop.get("amount")),
            TransactionType.from_int(Integer.parseInt(prop.get("type")))
        );

        m_bank.handleTransaction(T);
    }
}
