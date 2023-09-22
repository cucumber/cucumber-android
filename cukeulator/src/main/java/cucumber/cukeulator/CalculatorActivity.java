package cucumber.cukeulator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import cucumber.cukeulator.databinding.ActivityCalculatorBinding;

public class CalculatorActivity extends ComponentActivity {
    private enum Operation {ADD, SUB, MULT, DIV, NONE}
    private Operation operation;
    private boolean decimals;
    private boolean resetDisplay;
    private boolean performOperation;
    private double value;
    private ActivityCalculatorBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        operation = Operation.NONE;

        binding.btnD0.setOnClickListener(this::onDigitPressed);
        binding.btnD1.setOnClickListener(this::onDigitPressed);
        binding.btnD2.setOnClickListener(this::onDigitPressed);
        binding.btnD3.setOnClickListener(this::onDigitPressed);
        binding.btnD4.setOnClickListener(this::onDigitPressed);
        binding.btnD5.setOnClickListener(this::onDigitPressed);
        binding.btnD6.setOnClickListener(this::onDigitPressed);
        binding.btnD7.setOnClickListener(this::onDigitPressed);
        binding.btnD8.setOnClickListener(this::onDigitPressed);
        binding.btnD9.setOnClickListener(this::onDigitPressed);

        binding.btnOpAdd.setOnClickListener(v -> onOperatorPressed(v, Operation.ADD));
        binding.btnOpSubtract.setOnClickListener(v -> onOperatorPressed(v, Operation.SUB));
        binding.btnOpMultiply.setOnClickListener(v -> onOperatorPressed(v, Operation.MULT));
        binding.btnOpDivide.setOnClickListener(v -> onOperatorPressed(v, Operation.DIV));
        binding.btnOpEquals.setOnClickListener(v -> {
            if (performOperation) {
                performOperation();
                performOperation = false;
            }
            resetDisplay = true;
            value = getDisplayValue();
        });

        TextView txtCalcDisplay = binding.txtCalcDisplay;
        binding.btnSpecClear.setOnClickListener(v -> onSpecialPressed(() -> {
            value = 0;
            decimals = false;
            operation = Operation.NONE;
            txtCalcDisplay.setText(null);
            binding.txtCalcOperator.setText(null);
        }));

        binding.btnSpecComma.setOnClickListener(v -> onSpecialPressed(() -> {
            if (!decimals) {
                String text = displayIsEmpty() ? "0." : ".";
                txtCalcDisplay.append(text);
                decimals = true;
            }
        }));
        binding.btnSpecPercent.setOnClickListener(v -> onSpecialPressed(() -> {
            double value = getDisplayValue();
            double percent = value / 100.0F;
            txtCalcDisplay.setText(Double.toString(percent));
        }));
        binding.btnSpecSqroot.setOnClickListener(v -> onSpecialPressed(() -> {
            double value = getDisplayValue();
            double sqrt = Math.sqrt(value);
            txtCalcDisplay.setText(Double.toString(sqrt));
        }));
        binding.btnSpecPi.setOnClickListener(v -> onSpecialPressed(() -> {
            resetDisplay = false;
            binding.txtCalcOperator.setText(null);
            txtCalcDisplay.setText(Double.toString(Math.PI));
            if (operation != Operation.NONE) performOperation = true;
        }));
    }

    public void onDigitPressed(View v) {
        if (resetDisplay) {
            binding.txtCalcDisplay.setText(null);
            resetDisplay = false;
        }
        binding.txtCalcOperator.setText(null);

        if (decimals || !only0IsDisplayed()) binding.txtCalcDisplay.append(((TextView) v).getText());

        if (operation != Operation.NONE) performOperation = true;
    }

    public void onOperatorPressed(View v, Operation operation) {
        if (performOperation) {
            performOperation();
            performOperation = false;
        }
        this.operation = operation;
        binding.txtCalcOperator.setText(((TextView) v).getText());
        resetDisplay = true;
        value = getDisplayValue();
    }

    public void onSpecialPressed(Runnable action) {
        action.run();
        resetDisplay = false;
        performOperation = false;
    }

    private void performOperation() {
        double display = getDisplayValue();

        switch (operation) {
            case DIV:
                value = value / display;
                break;
            case MULT:
                value = value * display;
                break;
            case SUB:
                value = value - display;
                break;
            case ADD:
                value = value + display;
                break;
            case NONE:
                return;
            default:
                throw new RuntimeException("Unsupported operation.");
        }
        binding.txtCalcOperator.setText(null);
        binding.txtCalcDisplay.setText(Double.toString(value));
    }

    private boolean only0IsDisplayed() {
        CharSequence text = binding.txtCalcDisplay.getText();
        return text.length() == 1 && text.charAt(0) == '0';
    }

    private boolean displayIsEmpty() {
        return binding.txtCalcDisplay.getText().length() == 0;
    }

    private double getDisplayValue() {
        String display = binding.txtCalcDisplay.getText().toString();
        return display.isEmpty() ? 0.0F : Double.parseDouble(display);
    }
}
