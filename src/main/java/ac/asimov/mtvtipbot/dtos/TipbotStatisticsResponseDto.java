package ac.asimov.mtvtipbot.dtos;

public class TipbotStatisticsResponseDto {

    Integer totalTipbotUserCount;
    Integer totalTipbotTransactionCount;

    public TipbotStatisticsResponseDto(Integer totalTipbotUserCount, Integer totalTipbotTransactionCount) {
        this.totalTipbotUserCount = totalTipbotUserCount;
        this.totalTipbotTransactionCount = totalTipbotTransactionCount;
    }

    public Integer getTotalTipbotUserCount() {
        return totalTipbotUserCount;
    }

    public void setTotalTipbotUserCount(Integer totalTipbotUserCount) {
        this.totalTipbotUserCount = totalTipbotUserCount;
    }

    public Integer getTotalTipbotTransactionCount() {
        return totalTipbotTransactionCount;
    }

    public void setTotalTipbotTransactionCount(Integer totalTipbotTransactionCount) {
        this.totalTipbotTransactionCount = totalTipbotTransactionCount;
    }
}
