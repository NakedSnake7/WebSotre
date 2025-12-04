package com.WeedTitlan.server.dto;

import java.util.List;

public class OrderRequestDTO {
	   private String fullName;
	    private String email;
	    private String phone;
	    private String address;
	    private List<OrderItemDTO> items;
		public String getFullName() {
			return fullName;
		}
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getPhone() {
			return phone;
		}
		public void setPhone(String phone) {
			this.phone = phone;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public List<OrderItemDTO> getItems() {
			return items;
		}
		public void setItems(List<OrderItemDTO> items) {
			this.items = items;
		}

}
