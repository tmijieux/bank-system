import org.omg.CORBA.ORB;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import BankSystem.InterbankPOA;
import BankSystem.Transfer;
import BankSystem.Transaction;
import BankSystem.IBankTransaction;

import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public class InterbankServant
    extends InterbankPOA
{
    private class Bank
    {
        private int m_id;
        private IBankTransaction m_callback;
        private EventChannel m_chan;
        private ProxyPushConsumer m_ppconsumer;

        private final EventChannel createChannel()
        {
            org.omg.CORBA.Object chan_factory_ref = null;
            EventChannelFactory ev_chan_factory;
            try {
                chan_factory_ref =
                    m_ORB.resolve_initial_references("NotificationService");
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                e.printStackTrace();
                System.exit(1);
            }
            
            ev_chan_factory = EventChannelFactoryHelper.narrow(chan_factory_ref);
            org.omg.CORBA.IntHolder id = new org.omg.CORBA.IntHolder();
            Property[] props = new Property[0];
            EventChannel chan= null;
            try {
                chan = ev_chan_factory.create_channel(props, props, id);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return chan;
        }

        private ProxyPushConsumer getPushConsumer(EventChannel chan)
        {
            SupplierAdmin supp_adm;
            supp_adm = chan.default_supplier_admin();
            
            org.omg.CORBA.IntHolder supp_id = new org.omg.CORBA.IntHolder();
            ProxyConsumer generic_pconsumer = null;
            try {
                generic_pconsumer =  supp_adm.obtain_notification_push_consumer(
                    ClientType.ANY_EVENT, supp_id);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return ProxyPushConsumerHelper.narrow(generic_pconsumer);
        }
        
        public Bank(int id, IBankTransaction callback)
        {
            m_id = id;
            m_callback = callback;
            m_chan = createChannel();
            m_ppconsumer = getPushConsumer(m_chan);

            // client will instantiate a push_consumer implementation
            // and a proxy_push_supplier:
            m_callback.set_channel(m_chan);
        }

        public void sendTransaction(Transaction t)
        {
            org.omg.CORBA.Any ev = null;
            // build event:  ev.insert_data(...)

            try {
                m_ppconsumer.push(ev);
            } catch (org.omg.CosEventComm.Disconnected e) {
                e.printStackTrace();
                System.err.println("IMPOSSIBRRUUUUHH");
                // this object is supposed to in OUR process
                // connexion problems MUST NOT happen here !!
                // if it does, this is a programming mistake
                System.exit(1);
            }
        }
    }

    private class TransactionPrivate
    {
        public Transaction transac;
        public Transfer transfer;

        public TransactionPrivate(Transaction trc, Transfer trf)
        {
            transac = trc;
            transfer = trf;
        }
    }
    
    private ORB m_ORB;
    private HashMap<Integer, TransactionPrivate> m_transactionMap;
    private HashMap<Integer, Bank> m_bankMap;
    private List<Transfer> m_transferLog;
  
    private int m_lastTransactionId;
    private Object m_lastTransactionIdLock; 
        
    public InterbankServant(ORB orb)
    {
        m_ORB = orb;
        m_transactionMap = new HashMap<Integer, TransactionPrivate>();
        m_bankMap = new HashMap<Integer, Bank>();
        m_lastTransactionIdLock = new Object();
        m_transferLog = new LinkedList<Transfer>();
    }

    public void register(int bank_id, IBankTransaction trans)
    {
        m_bankMap.put(bank_id, new Bank(bank_id, trans));
    }

    public void confirm(int transaction_id, boolean failure)
    {
        TransactionPrivate pTrans;
        pTrans = m_transactionMap.get(transaction_id);

        if (pTrans != null) {
            if (pTrans.transac.isDebit) {
                // get src bank; make credit transaction end;
                /*TODO*/;
                
                // log transfer
                m_transferLog.add(pTrans.transfer);
            } else
                ;//throw new Exception();
        }
    }

    public void transfer_request(Transfer t)
    {
        Bank dst;
        dst = m_bankMap.get(t.dest_bank_id);
        if (dst != null) {
            int id;
            synchronized(m_lastTransactionIdLock) {
                id = m_lastTransactionId ++;
            }

            Transaction trans;
            trans = new Transaction(id, t.dest_bank_id, true, t.amount);
            m_transactionMap.put(id, new TransactionPrivate(trans, t));
            dst.sendTransaction(trans); // --> push event
        }
    }
}
