import org.omg.CORBA.ORB;
import java.util.HashMap;
import BankSystem.InterbankPOA;
import BankSystem.Transfer;
import BankSystem.Transaction;
import BankSystem.IBankTransaction;

private class Bank
{
  private int m_id;
  private IBankTransaction m_callback;
  private EventChannel m_chan;
  private PushSupplier m_supp;

  public Bank(int id, IBankTransaction callback)
  {
      m_id = id;
      m_callback = callback;

      m_chan = new EventChannel();
      m_supp = m_chan.get_default_supplier();
        
      trans.setReceiveChannel(ec); // client create push_consumer
  }

  public void sendTransaction(Transaction t)
  {
      StructuredEvent ev;
      // build event:  ev.insert_data(...)
      m_supp.push_structured_event(ev);
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


public class InterbankServant
    extends InterbankPOA
{
  private ORB m_ORB;
  private HashMap<Integer, TransactionPrivate> m_transactionMap;
  private HashMap<Integer, Bank> m_bankMap;
  
  private int m_lastTransactionId;
  private Object m_lastTransactionIdLock; 
        
  public InterbankServant(ORB orb)
  {
      m_ORB = orb;
      m_transactionMap = new HashMap<Integer, TransactionPrivate>();
      m_bankMap = new HashMap<Integer, Bank>();
      m_lastTransactionIdLock = new Object();
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
          if (pTrans.transac.isDebit)
              // get src bank; make credit transaction end;
              // AND log transfer
              ;
          else
              throw new Exception();
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
