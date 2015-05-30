package aero.xpand;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class IpAddressDialogFragment extends DialogFragment {

	String message;

	public IpAddressDialogFragment(String message) {
		this.message = message;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message).setTitle("Device's IP address :")
				.setNeutralButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// close the dialog
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}