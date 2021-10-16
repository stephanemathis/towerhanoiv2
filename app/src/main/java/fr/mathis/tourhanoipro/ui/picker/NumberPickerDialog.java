package fr.mathis.tourhanoipro.ui.picker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import fr.mathis.tourhanoipro.R;

public class NumberPickerDialog extends DialogFragment {

    private int mCurrentValue;

    public NumberPickerDialog(int currentValue)  {
        super();

        mCurrentValue = currentValue;
    }

    private NumberPicker.OnValueChangeListener valueChangeListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final NumberPicker numberPicker = new NumberPicker(getActivity());

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(21);
        numberPicker.setValue(mCurrentValue);

        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return transformValues(value) + "";
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.disk_nb_picker_title);

        builder.setPositiveButton(R.string.disk_nb_picker_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker, transformValues(numberPicker.getValue()), transformValues(numberPicker.getValue()));
            }
        });

        builder.setNegativeButton(R.string.disk_nb_picker_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    private int transformValues(int value) {
        return value == 21 ? 70 : value;

    }
}