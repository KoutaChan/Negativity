package com.elikill58.negativity.universal;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.adapter.Adapter;

public abstract class NegativityPlayer {

	private final UUID playerId;
	private boolean isBanned = false, isMcLeaks = false, showAlert = true;
	// To remove multiple ban at follow
	private boolean isInBanning = false;

	public NegativityPlayer(UUID playerId, String playerName) {
		this.playerId = playerId;
		Adapter ada = Adapter.getAdapter();
		ada.isUsingMcLeaks(playerId).thenAccept(isUsingMcLeaks -> {
			this.isMcLeaks = isUsingMcLeaks;
		});
		getAccount().setPlayerName(playerName);
		ada.getAccountManager().save(playerId);
	}

	public NegativityAccount getAccount() {
		return NegativityAccount.get(playerId);
	}
	
	public UUID getUUID() {
		return playerId;
	}
	
	public boolean isMcLeaks() {
		return isMcLeaks;
	}
	
	public void setMcLeaks(boolean isMcLeaks) {
		this.isMcLeaks = isMcLeaks;
	}
	
	public boolean isBanned() {
		return isBanned;
	}
	
	public void setBanned(boolean b) {
		isBanned = b;
	}
	
	public boolean isInBanning() {
		return isInBanning;
	}
	
	public void setInBanning(boolean b) {
		isInBanning = b;
	}

	public int getWarn(Cheat c) {
		return getAccount().getWarn(c);
	}

	public int getAllWarn(Cheat c) {
		return getAccount().getWarn(c);
	}
	
	public boolean isShowAlert() {
		return showAlert;
	}

	public void setShowAlert(boolean showAlert) {
		this.showAlert = showAlert;
	}

	public abstract Object getPlayer();
	public abstract boolean hasDefaultPermission(String s);
	public abstract double getLife();
	public abstract String getName();
	public abstract String getGameMode();
	public abstract float getWalkSpeed();
	public abstract int getLevel();
	public abstract void kickPlayer(String reason, String time, String by, boolean def);
	public abstract void banEffect();
	public abstract void startAnalyze(Cheat c);
	public abstract void startAllAnalyze();
	public abstract void stopAnalyze(Cheat c);
	public abstract boolean isOp();
	public abstract String getIP();
	public abstract String getReason(Cheat c);
	public abstract @Nullable Version getPlayerVersion();

}
