package com.example.loom_group_2.ui;

// Call this method when your Directions API returns data
private void displayRoutes(JSONObject jsonResponse) {
    List<RouteModel> routes = new ArrayList<>();
    try {
        JSONArray jsonRoutes = jsonResponse.getJSONArray("routes");

        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject routeObj = jsonRoutes.getJSONObject(i);
            JSONObject leg = routeObj.getJSONArray("legs").getJSONObject(0);

            String summary = routeObj.getString("summary");
            int duration = leg.getJSONObject("duration").getInt("value"); // seconds
            double distance = leg.getJSONObject("distance").getDouble("value"); // meters

            // Fetch KPL from your user's current vehicle (Budgetarian data)
            double kpl = currentVehicle != null ? currentVehicle.getKpl() : 15.0;

            routes.add(new RouteModel(summary, duration, distance, kpl));
        }

        runOnUiThread(() -> {
            RouteAdapter adapter = new RouteAdapter(routes);
            recyclerView.setAdapter(adapter);
        });

    } catch (Exception e) {
        e.printStackTrace();
    }
}