package ml.ikwid.transplantsmp.client;

import ml.ikwid.transplantsmp.TransplantSMP;
import ml.ikwid.transplantsmp.client.screen.ChooseTransplantScreen;
import ml.ikwid.transplantsmp.common.TransplantType;
import ml.ikwid.transplantsmp.common.networking.NetworkingConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class TransplantSMPClient implements ClientModInitializer {
	public static TransplantType transplantType;
	public static int transplants = 0;

	@Override
	public void onInitializeClient() {
		TransplantSMP.LOGGER.info("hello medical patient, welcome to the deadliest minecraft smp aka a hospital");

		ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UPDATE_ORGAN_COUNT, (client, handler, buf, responseSender) -> {
			int count = buf.readInt();
			client.execute(() -> {
				transplants = count;
				TransplantSMP.LOGGER.info("count updated -client, count = " + count);
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UPDATE_TRANSPLANT_TYPE, (client, handler, buf, responseSender) -> {
			String type = buf.readString();
			client.execute(() -> {
				transplantType = TransplantType.get(type);
				client.setScreen(null);

				TransplantSMP.LOGGER.info("type updated -client, type = " + type);
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.NEEDS_TRANSPLANT, (client, handler, buf, responseSender) -> client.execute(() -> client.setScreen(new ChooseTransplantScreen())));
	}
}
