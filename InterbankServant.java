import org.omg.CORBA.ORB;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import BankSystem.InterbankPOA;
import BankSystem.Transfer;
import BankSystem.TransferDate;
import BankSystem.Transaction;
import BankSystem.IBankTransaction;

import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public class InterbankServant
    extends InterbankPOA
{
    private class PTransaction
    { /** this class's role is just to retain a transfer
       *  associated to a transaction before 'dst_bank'
       *  eventually confirm its transaction     */
        public Transaction transac;
        public Transfer transfer;

        public PTransaction(Transaction trc, Transfer trf)
        {
            transac = trc;
            transfer = trf;
        }
    }

    private ORB m_ORB;
    private HashMap<Integer, PTransaction> m_transactionMap;
    private HashMap<Integer, BankProxy> m_bankMap;
    private List<TransferDate> m_transferLog;

    private int m_lastTransactionId;
    private Object m_lastTransactionIdLock;

    public InterbankServant(ORB orb)
    {
        m_ORB = orb;
        m_transactionMap = new HashMap<Integer, PTransaction>();
        m_bankMap = new HashMap<Integer, BankProxy>();
        m_lastTransactionIdLock = new Object();
        m_transferLog = new LinkedList<TransferDate>();
    }

    public void register(int bank_id, IBankTransaction trans)
    {
        // if (m_bankMap.get(bank_id) == null)
        m_bankMap.put(bank_id, new BankProxy(bank_id, trans, m_ORB));
    }

    public void confirm(int transaction_id, boolean failure)
    {
        PTransaction pTrans;
        pTrans = m_transactionMap.get(transaction_id);

        if (pTrans != null) {
            Transaction T = pTrans.transac;
            Transfer t = pTrans.transfer;

            if (T.isDebit) {
                BankProxy src;
                src = m_bankMap.get(t.src_bank_id);
                if (src != null) {
                    m_transferLog.add(new TransferDate(t, new Date().getTime()));
                    Transaction trans;
                    trans = new Transaction(T.transactionID, t.src_bank_id,
                                            false, t.amount);
                    m_transactionMap.remove(pTrans);
                    src.sendTransaction(trans);
                }
            } else
                throw new RuntimeException(
                    "BRUH: some bank try to confirm a transfer"+
                    " which benefit to the very same bank");
        }
    }

    public void transfer_request(Transfer t)
    {
        BankProxy dst;
        dst = m_bankMap.get(t.dest_bank_id);
        if (dst != null) {
            int id;
            synchronized(m_lastTransactionIdLock) {
                id = m_lastTransactionId ++;
            }

            Transaction trans;
            trans = new Transaction(id, t.dest_bank_id, true, t.amount);
            m_transactionMap.put(id, new PTransaction(trans, t));
            dst.sendTransaction(trans); // --> push event
        }
    }

    public TransferDate[] get_transfer_log()
    {
        return m_transferLog.toArray(new TransferDate[m_transferLog.size()]);
    }
}
