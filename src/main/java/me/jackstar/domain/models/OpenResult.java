package me.jackstar.drakescrates.domain.models;

public class OpenResult {
    private final boolean success;
    private final String message;
    private final Reward winningReward;

    public OpenResult(boolean success, String message, Reward winningReward) {
        this.success = success;
        this.message = message;
        this.winningReward = winningReward;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Reward getWinningReward() {
        return winningReward;
    }

    public static OpenResult failure(String message) {
        return new OpenResult(false, message, null);
    }

    public static OpenResult success(Reward reward) {
        return new OpenResult(true, "Crate opened successfully", reward);
    }
}
