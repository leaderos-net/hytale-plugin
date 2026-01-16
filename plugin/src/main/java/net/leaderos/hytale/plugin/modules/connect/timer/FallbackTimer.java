package net.leaderos.hytale.plugin.modules.connect.timer;

import com.hypixel.hytale.server.core.HytaleServer;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.api.ModuleManager;
import net.leaderos.hytale.plugin.modules.connect.ConnectModule;
import net.leaderos.hytale.shared.model.request.GetRequest;
import net.leaderos.hytale.shared.model.request.PostRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Time checker for update scheduler
 *
 */
public class FallbackTimer {

    /**
     * Runnable id for cancel or resume
     */
    public static ScheduledFuture<?> task;
    public static void run() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }

        if (LeaderosPlugin.getInstance().getModulesFile().getConnect().getFallbackTimer() > 0) {
            task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    () -> {
                        // Runs every X minutes after the previous execution completes
                        try {
                            // Fetch commands in queue
                            String serverToken = LeaderosPlugin.getInstance().getModulesFile().getConnect().getServerToken();
                            GetRequest request = new GetRequest("command-logs/" + serverToken + "/queue");
                            JSONObject response = request.getResponse().getResponseMessage();
                            List<String> logIDs = new ArrayList<>();
                            response.getJSONArray("array").forEach(queue -> {
                                try {
                                    JSONObject queueObject = (JSONObject) queue;
                                    int retry = 0;
                                    long timeDiff = 0;
                                    try {
                                        retry = Integer.parseInt(queueObject.getString("retry"));
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date date = sdf.parse(queueObject.getString("updatedAt"));
                                        timeDiff = (System.currentTimeMillis() - date.getTime()) / 1000;
                                    } catch (Exception ignored) {}

                                    // Only add error or stuck commands
                                    if (
                                            queueObject.getString("status").equals("error") ||
                                                    (queueObject.getString("status").equals("sending") && retry < 60 && timeDiff > 60 && timeDiff < 86400)
                                    ) {
                                        logIDs.add(queueObject.getString("id"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                            if (!logIDs.isEmpty()) {
                                // Validate commands
                                Map<String, String> formData = new HashMap<>();
                                formData.put("token", serverToken);
                                for (int i = 0; i < logIDs.size(); i++) {
                                    formData.put("commands[" + i + "]", logIDs.get(i));
                                }
                                PostRequest postRequest = new PostRequest("command-logs/validate", formData);
                                JSONObject validateResponse = postRequest.getResponse().getResponseMessage();

                                if (validateResponse.has("commands")) {
                                    JSONArray commandsJSON = validateResponse.getJSONArray("commands");
                                    List<String> commandsList = new ArrayList<>();
                                    String username = "";

                                    for (Object item : commandsJSON) {
                                        if (item instanceof JSONObject) {
                                            JSONObject jsonItem = (JSONObject) item;
                                            commandsList.add(jsonItem.getString("command"));
                                            if (username.isEmpty() && jsonItem.has("username")) {
                                                username = jsonItem.getString("username");
                                            }
                                        }
                                    }

                                    // Execute validated commands
                                    ConnectModule connectModule = (ConnectModule) ModuleManager.getModule("Connect");
                                    connectModule.getSocket().executeCommands(commandsList, username);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    LeaderosPlugin.getInstance().getModulesFile().getConnect().getFallbackTimer(), LeaderosPlugin.getInstance().getModulesFile().getConnect().getFallbackTimer(), TimeUnit.MINUTES
            );
        }
    }
}
