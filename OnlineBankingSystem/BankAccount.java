import java.math.BigDecimal;

public class BankAccount {

    private int accountId;
    private int userId;
    private String accountNumber;
    private BigDecimal balance;

    public BankAccount(int accountId, int userId,
                       String accountNumber, BigDecimal balance) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
