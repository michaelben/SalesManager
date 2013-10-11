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

//The original ArrayAdapter has too big item width
public class CategoryAdapter extends ArrayAdapter<Category> {

	private List<Category> categoryList;
	private Context context;

	public CategoryAdapter(List<Category> categoryList, Context ctx) {
		super(ctx, android.R.layout.simple_spinner_dropdown_item, categoryList);
		this.categoryList = categoryList;
		this.context = ctx;
	}

	public int getCount() {
		return categoryList.size();
	}

	public Category getItem(int position) {
		return categoryList.get(position);
	}

	public long getItemId(int position) {
		return categoryList.get(position).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		CategoryHolder holder = new CategoryHolder();

		// First let's verify the convertView is not null
		if (convertView == null) {
			// This a new view we inflate the new layout
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			// Now we can fill the layout with the right values
			TextView tv = (TextView) v.findViewById(R.id.name);

			holder.categoryNameView = tv;

			v.setTag(holder);
		}
		else 
			holder = (CategoryHolder) v.getTag();

		Category p = categoryList.get(position);
		holder.categoryNameView.setText(p.toString());

		return v;
	}

	private static class CategoryHolder {
		public TextView categoryNameView;
	}
}