package org.rohm1.androsat;

import java.util.ArrayList;

import org.rohm1.androsat.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements AndroSATListener {
	EditText inputBox = null;
	TextView outputBox = null;
	CheckBox debugBox = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputBox = (EditText) findViewById(R.id.inputBox);

        inputBox.setText(" c Here is a comment." + "\n" +
				" p cnf 5 3" + "\n" +
				" 1 -5 4 0" + "\n" +
				" -1 5 3 4 0" + "\n" +
				" -3 -4 0");
        outputBox = (TextView) findViewById(R.id.outputBox);
        debugBox = (CheckBox) findViewById(R.id.debugBox);
    }

    public void launchMinisat(View view) {
    	AndroSAT a = new AndroSAT(this, inputBox.getText().toString());
    	a.setDebug(debugBox.isChecked());
    	a.cleanCNF();
    	a.solve();
    }

	@Override
	public void onSATResult(ArrayList<Integer> result, String[] output) {
		this.outputBox.setText("Result:\n");
		for(int i = 0 ; i < result.size() ; i++)
			this.outputBox.append(result.get(i).toString() + " ");
		this.outputBox.append("\nOutput:\n");
		for(String s : output)
			this.outputBox.append(s + "\n");
	}



}
