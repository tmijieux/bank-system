import org.omg.CORBA.ORB;
import java.util.HashMap;
import BankSystem.InterbankPOA;
import BankSystem.Transfer;
import BankSystem.Transaction;
import BankSystem.IBankTransaction;

public class InterbankServant
    extends InterbankPOA
{
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
    private HashMap<Integer, IBankTransaction> m_bankMap;
    private int m_lastTransactionId;
    private Object m_lastTransactionIdLock; 
        
    public InterbankServant(ORB orb)
    {
        m_ORB = orb;
        m_transactionMap = new HashMap<Integer, TransactionPrivate>();
        m_bankMap = new HashMap<Integer, IBankTransaction>();
        m_lastTransactionIdLock = new Object();
    }

    public void register(int bank_id, IBankTransaction trans)
    {
        m_bankMap.put(bank_id, trans);
    }

    public void confirm(int transaction_id, boolean failure)
    {
        TransactionPrivate pTrans;
        pTrans = m_transactionMap.get(transaction_id);
    }
    
    public void transfer_request(Transfer t)
    {
        IBankTransaction dst;
        dst = m_bankMap.get(t.dest_bank_id);
        if (dst != null) {
            int id;
            synchronized(m_lastTransactionIdLock) {
                id = m_lastTransactionId ++;
            }

            Transaction trans;
            trans = new Transaction(id, t.dest_bank_id, true, t.amount);
            m_transactionMap.put(id, new TransactionPrivate(trans, t));
            
            dst.do_transaction(trans); // --> push event
        }
    }
}
