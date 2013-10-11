/*
 * Copyright (C) 2013 michael.zhang (http://www.hubblewave.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class CustomerAdapter extends ArrayAdapter<Customer> implements Filterable {

	private List<Customer> customerList;
	private Context context;
	private Filter customerFilter;
	private List<Customer> origCustomerList;

	public CustomerAdapter(List<Customer> customerList, Context ctx) {
		super(ctx, android.R.layout.simple_list_item_1, customerList);
		this.customerList = customerList;
		this.context = ctx;
		this.origCustomerList = customerList;
	}

	public int getCount() {
		return customerList.size();
	}

	public Customer getItem(int position) {
		return customerList.get(position);
	}

	public long getItemId(int position) {
		return customerList.get(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		CustomerHolder holder = new CustomerHolder();

		// First let's verify the convertView is not null
		if (convertView == null) {
			// This a new view we inflate the new layout
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(android.R.layout.simple_list_item_1, null);
			// Now we can fill the layout with the right values
			TextView tv = (TextView) v.findViewById(android.R.id.text1);

			holder.customerNameView = tv;

			v.setTag(holder);
		}
		else 
			holder = (CustomerHolder) v.getTag();

		Customer p = customerList.get(position);
		holder.customerNameView.setText(p.toString());

		return v;
	}

	public void resetData() {
		customerList = origCustomerList;
	}

	private static class CustomerHolder {
		public TextView customerNameView;
	}

	@Override
	public Filter getFilter() {
		if (customerFilter == null)
			customerFilter = new CustomerFilter();

		return customerFilter;
	}

	private class CustomerFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			
			if (constraint == null || constraint.length() == 0) {
				results.values = origCustomerList;
				results.count = origCustomerList.size();
			}
			else {
				List<Customer> nCustomerList = new ArrayList<Customer>();

				for (Customer p : customerList) {
					if (p.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
						nCustomerList.add(p);
				}

				results.values = nCustomerList;
				results.count = nCustomerList.size();

			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			if (results.count == 0)
				notifyDataSetInvalidated();
			else {
				customerList = (List<Customer>) results.values;
				notifyDataSetChanged();
			}
		}
	}
}