/*
Copyright (c) 2012, rohm1 <rp@rohm1.com>.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

* Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.

* Neither the name of rohm1 nor the names of his
contributors may be used to endorse or promote products derived
from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package org.rohm1.androsat;

import java.util.ArrayList;

import org.rohm1.androsat.AndroSATListener;

import android.os.AsyncTask;

/**
 * Class to manipulate the native C++ MiniSat
 *
 * There are small differences between the official CNF and the one used
 * here. You cannot use comment, the line that specifies the number of
 * variables and clauses is reduced to the number of variables and
 * clauses followed by a zero, and everything is on the same line.<br />
 * Example:<br />
 * <pre> c Here is a comment.
 p cnf 5 3
 1 -5 4 0
 -1 5 3 4 0
 -3 -4 0</pre>
 * becomes:
 * <pre>5 3 0 1 -5 4 0 -1 5 3 4 0 -3 -4 0</pre>
 *
 * Use {@link #cleanCNF()} to convert your CNF from the official format
 * to the one used here.
 *
 * @author rohm1
 * @create 04/12/2012
 */
public class AndroSAT {

	/**
	 * List of listeners
	 * 		They will be notified when MiniSat is done computing
	 */
	protected ArrayList<AndroSATListener> listeners;

	/**
	 * The CNF clauses to send to MiniSat
	 */
	protected String cnf;

	/**
	 * If set to true, MiniSat will send back more information.
	 * 		It can be used to check MiniSat has correctly understood each clause.
	 */
	protected Boolean debug;

	/**
	 * Constructor
	 *
	 * @param l a listener
	 */
	public AndroSAT(AndroSATListener l) {
		this.listeners = new ArrayList<AndroSATListener>();
		this.listeners.add(l);
		this.cnf = null;
		this.debug = false;
	}

	/**
	 * Constructor
	 *
	 * @param l a listener
	 * @param cnf clauses to solve
	 */
	public AndroSAT(AndroSATListener l, String cnf) {
		this(l);
		this.cnf = cnf;
	}

	/**
	 * Adds a listener
	 *
	 * @param l a listener to add
	 */
	public void addListener(AndroSATListener l) {
		this.listeners.add(l);
	}

	/**
	 * Removes a listener
	 *
	 * @param l a listener to remove
	 * @return true if this {@link #listeners} is modified, false otherwise
	 */
	public Boolean removeListener(AndroSATListener l) {
		return this.listeners.remove(l);
	}

	/**
	 * Notifies the listeners when MiniSat is done computing
	 *
	 * @param result a list containing the variables in the new state computed by MiniSAT
	 * @param output MiniSAT's output
	 */
	protected void notifyListeners(ArrayList<Integer> result, String[] output) {
		for(AndroSATListener l : listeners)
			l.onSATResult(result, output);
	}

	static {
		System.loadLibrary("minisat");
	}

	/**
	 * Wrapper for the native MiniSat
	 *
	 * @param cnf clauses
	 * @param debug {@link #debug}
	 * @return MiniSat's output
	 */
	public native String minisatJNI(String cnf, int debug);

	/**
	 * Invokes MiniSat to solve the given {@link #cnf}
	 *
	 * The computation is done in an {@link android.os.AsyncTask.AsyncTask AsyncTask}
	 * so that it is not blocking. {@link #notifyListeners(ArrayList, String[]) notifyListeners()} is then called.
	 */
	public void solve() {
		new AsyncTask<Void, Void, Void>() {
			protected String[] output;
			protected ArrayList<Integer> result;

			@Override
			public Void doInBackground(Void... params) {
				String o = minisatJNI(cnf, debug ? 1 : 0);

				this.result = new ArrayList<Integer>();
				this.output = o.split("\n");
				for(int i = 0 ; i < this.output.length ; i++) {
					if(this.output[i].equals("SAT")) {
						String[] resultLine = this.output[i+1].split(" ");
						for(String term : resultLine) {
							try {
								int iTerm = Integer.parseInt(term);
								if(iTerm != 0)
									this.result.add(iTerm);
							} catch(NumberFormatException e) {}
						}
						break;
					}
				}

				return null;
			}

			@Override
			public void onPostExecute(Void _result) {
				notifyListeners(result, output);
			}

		}.execute();
	}

	/**
	 * Set the cnf
	 *
	 * @param cnf
	 */
	public void setCNF(String cnf) {
		this.cnf = cnf;
	}

	/**
	 * Sets debug
	 *
	 * @param debug
	 */
	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	/**
	 * Cleans the formula in the official CNF format to the format used here.
	 */
	public void cleanCNF() {
		StringBuffer s = new StringBuffer();
		String[] cnf = this.cnf.split("\n");

		for(String clause : cnf) {
			clause = clause.trim();
			if(clause.substring(0, 1).equals("p")) {
				String[] params = clause.split(" ");
				for(String param : params) {
					try {
						int i = Integer.parseInt(param);
						s.append(String.valueOf(i) + " ");
					} catch(NumberFormatException e) {}
				}
				s.append("0");
			}
			else if(clause.substring(0, 1).equals("c"));
			else
				s.append(" " + clause);
		}

		this.cnf = s.toString();
	}

}
