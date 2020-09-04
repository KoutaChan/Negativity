package com.elikill58.negativity.common.protocols;

import java.util.List;

import com.elikill58.negativity.api.GameMode;
import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.block.Block;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventListener;
import com.elikill58.negativity.api.events.Listeners;
import com.elikill58.negativity.api.events.block.BlockBreakEvent;
import com.elikill58.negativity.api.events.negativity.PlayerPacketsClearEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.potion.PotionEffectType;
import com.elikill58.negativity.api.utils.ItemUtils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.PacketType;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class Nuker extends Cheat implements Listeners {

	public Nuker() {
		super(CheatKeys.NUKER, true, Materials.BEDROCK, CheatCategory.WORLD, true, "breaker", "bed breaker", "bedbreaker");
	}
	
	@EventListener
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this))
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		Block b = e.getBlock();
		if(p.hasPotionEffect(PotionEffectType.FAST_DIGGING) || b == null || !b.getType().isSolid() || isInstantBlock(b.getType().getId()))
			return;
		int ping = p.getPing();
		if(checkActive("distance")) {
			List<Block> target = p.getTargetBlock(5);
			if(!target.isEmpty()) {
				for(Block targetBlock : target) {
					double distance = targetBlock.getLocation().distance(e.getBlock().getLocation());
					if ((targetBlock.getType() != e.getBlock().getType()) && distance > 3.5 && targetBlock.getType() != Materials.AIR) {
						boolean mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(distance * 15 - ping), "distance",
								"BlockDig " + b.getType().getId() + ", player see " + targetBlock.getType().getId() + ". Distance between blocks " + distance + " block. Ping: " + ping + ". Warn: " + np.getWarn(this));
						if(isSetBack() && mayCancel)
							e.setCancelled(true);
					}
				}
			}
		}
		if(checkActive("time")) {
			long temp = System.currentTimeMillis(), dis = temp - np.LAST_BLOCK_BREAK;
			if(dis < 50 && !ItemUtils.hasDigSpeedEnchant(p.getItemInHand()) && !p.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
				boolean mayCancel = Negativity.alertMod(ReportType.VIOLATION, p, this, (int) (100 - dis), "time",
						"Type: " + e.getBlock().getType().getId() + ". Last: " + np.LAST_BLOCK_BREAK + ", Now: " + temp + ", diff: " + dis + " (ping: " + ping + "). Warn: " + np.getWarn(this), hoverMsg("breaked_in", "%time%", dis));
				if(isSetBack() && mayCancel)
					e.setCancelled(true);
			}
			np.LAST_BLOCK_BREAK = temp;
		}
	}
	
	private boolean isInstantBlock(String m) {
		if(m.contains("SLIME") || m.contains("TNT") || m.contains("LEAVE"))
			return true;
		return false;
	}

	
	@EventListener
	public void onPacketClear(PlayerPacketsClearEvent e) {
		NegativityPlayer np = e.getNegativityPlayer();
		if(np.hasDetectionActive(this) && checkActive("packet")) {
			Player p = e.getPlayer();
			int ping = p.getPing();
			int blockDig = e.getPackets().getOrDefault(PacketType.Client.BLOCK_DIG, 0);
			if(ping < getMaxAlertPing() && (blockDig - (ping / 10)) > 20 && !ItemUtils.hasDigSpeedEnchant(p.getItemInHand()))
				Negativity.alertMod(blockDig > 200 ? ReportType.VIOLATION : ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(20 + blockDig),
						"packet", "BlockDig packet: " + blockDig + ", ping: " + ping + " Warn for Nuker: " + np.getWarn(this));
		}
	}
}
