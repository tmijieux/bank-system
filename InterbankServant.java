import org.omg.CORBA.ORB;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

import BankSystem.*;

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
        public Transaction T;
        public Transfer    F;

        public PTransaction(Transaction T, Transfer F)
        {
            this.T = T;
            this.F = F;
        }
    }

    private ORB m_ORB;
    private HashMap<Integer, PTransaction> m_transactionMap;
    private HashMap<Integer, BankProxy> m_bankMap;
    private List<TransferDate> m_transferHistory;

    private int m_lastTransactionId;
    private Object m_lastTransactionIdLock;

    public InterbankServant(ORB orb)
    {
        m_ORB = orb;
        m_transactionMap = new HashMap<Integer, PTransaction>();
        m_bankMap = new HashMap<Integer, BankProxy>();
        m_lastTransactionIdLock = new Object();
        m_transferHistory = new LinkedList<TransferDate>();
    }

    public void register(int bank_id, Bank_IInterbank bank_callback)
    {
        m_bankMap.put(bank_id, new BankProxy(bank_id, bank_callback, m_ORB));
    }

    private void addToHistory(Transfer F)
    {
        TransferDate TD = new TransferDate(F, new Date().getTime());
        m_transferHistory.add(TD);
    }

    private void sendConfirmTransaction(
        BankProxy bank, Transaction T, Transfer F)
    {
        Transaction CT;
        CT = new Transaction(T.id, F.src_bank_id, F.src_account_id,
                             F.amount, TransactionType.CREDIT);
        bank.sendTransaction(CT);
    }

    public void confirm_transaction(int id, boolean failure)
    {
        PTransaction pt = m_transactionMap.get(id);

        if (pt != null) {
            Transaction T = pt.T;
            Transfer    F = pt.F;

            if (T.type == TransactionType.DEBIT) {
                BankProxy src = m_bankMap.get(F.src_bank_id);
                if (src != null) {
                    m_transactionMap.remove(pt);

                    addToHistory(F);
                    sendConfirmTransaction(src, T, F);
                }
            } else
                throw new RuntimeException(
                    "BRUUHH: some bank try to confirm a transfer "+
                    "which benefit to the very same bank. BAD BANK !!");
        }
    }

    public void request_transfer(Transfer F)
    {
        BankProxy dst;
        dst = m_bankMap.get(F.dest_bank_id);
        if (dst != null) {
            int id;
            synchronized(m_lastTransactionIdLock) {
                id = m_lastTransactionId ++;
            }

            Transaction T;
            T = new Transaction(id, F.dest_bank_id, F.dest_account_id,
                                F.amount, TransactionType.DEBIT);
            m_transactionMap.put(id, new PTransaction(T, F));
            dst.sendTransaction(T);
        }
    }

    public TransferDate[] get_transfer_history()
    {
        return m_transferHistory.toArray(
            new TransferDate[m_transferHistory.size()]);
    }
}
