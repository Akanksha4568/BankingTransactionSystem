
package com.banking.model;

import java.util.List;

public class TransactionHistoryPageResponse {

    private List<TransactionHistoryResponse> transactions;
    private int currentPage;
    private int pageSize;
    private int totalTransactions;
    private int totalPages;

    public TransactionHistoryPageResponse() {
    }

    public TransactionHistoryPageResponse(
            List<TransactionHistoryResponse> transactions,
            int currentPage,
            int pageSize,
            int totalTransactions) {

        this.transactions = transactions;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalTransactions = totalTransactions;

        this.totalPages = (int) Math.ceil(
                (double) totalTransactions / pageSize
        );
    }

    public List<TransactionHistoryResponse> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<TransactionHistoryResponse> transactions) {
        this.transactions = transactions;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
