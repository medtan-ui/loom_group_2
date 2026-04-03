package com.example.loom_group_2.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.logic.VehicleRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class AddCustomVehicleSheet extends BottomSheetDialogFragment {

    public interface OnVehicleSavedListener {
        void onVehicleSaved(Motorcycle vehicle);
    }

    private OnVehicleSavedListener listener;
    private TextInputEditText etMake, etModel, etYear, etKpl, etNickname;
    private TextInputLayout tilMake, tilModel, tilYear, tilKpl;
    private Spinner spinnerTransmission;

    public void setOnVehicleSavedListener(OnVehicleSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_custom_vehicle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMake = view.findViewById(R.id.etMake);
        etModel = view.findViewById(R.id.etModel);
        etYear = view.findViewById(R.id.etYear);
        etKpl = view.findViewById(R.id.etKpl);
        etNickname = view.findViewById(R.id.etNickname);
        
        tilMake = view.findViewById(R.id.tilMake);
        tilModel = view.findViewById(R.id.tilModel);
        tilYear = view.findViewById(R.id.tilYear);
        tilKpl = view.findViewById(R.id.tilKpl);
        
        spinnerTransmission = view.findViewById(R.id.spinnerTransmission);
        Button btnSave = view.findViewById(R.id.btnSave);

        String[] transmissions = {"Manual", "Automatic"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, transmissions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransmission.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveVehicle());
    }

    private void saveVehicle() {
        if (!validate()) return;

        String make = etMake.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        int year = Integer.parseInt(etYear.getText().toString().trim());
        double kpl = Double.parseDouble(etKpl.getText().toString().trim());
        String nickname = etNickname.getText().toString().trim();
        String transmission = spinnerTransmission.getSelectedItem().toString();
        
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Motorcycle customVehicle = new Motorcycle(uid, make, model, year, kpl, transmission, nickname);

        VehicleRepository.getInstance(requireContext()).insertCustomVehicle(customVehicle, id -> {
            customVehicle.setId(id.intValue());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (listener != null) listener.onVehicleSaved(customVehicle);
                    dismiss();
                });
            }
        });
    }

    private boolean validate() {
        boolean isValid = true;
        
        if (TextUtils.isEmpty(etMake.getText())) {
            tilMake.setError("Make is required");
            isValid = false;
        } else {
            tilMake.setError(null);
        }

        if (TextUtils.isEmpty(etModel.getText())) {
            tilModel.setError("Model is required");
            isValid = false;
        } else {
            tilModel.setError(null);
        }

        String yearStr = etYear.getText().toString();
        if (TextUtils.isEmpty(yearStr)) {
            tilYear.setError("Year is required");
            isValid = false;
        } else {
            int year = Integer.parseInt(yearStr);
            if (year < 1900 || year > 2100) {
                tilYear.setError("Enter a valid year (1900-2100)");
                isValid = false;
            } else {
                tilYear.setError(null);
            }
        }

        String kplStr = etKpl.getText().toString();
        if (TextUtils.isEmpty(kplStr)) {
            tilKpl.setError("KPL is required");
            isValid = false;
        } else {
            double kpl = Double.parseDouble(kplStr);
            if (kpl <= 0) {
                tilKpl.setError("KPL must be greater than 0");
                isValid = false;
            } else {
                tilKpl.setError(null);
            }
        }

        return isValid;
    }
}
