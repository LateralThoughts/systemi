This is Fac-Stemi
=====================================


Goal is simple : generate invoices using LT templates from structured data.

Example :

Request
-------

POST : 
Content-type : application/json
Body : {
	"title" : "Facture de novembre",
	"invoiceNumber": "VT055",
	"client" : {
		"name" : "VIDAL",
		"address" : "27 rue camille desmoulins",
		"postalCode" : 94550,
		"city": "Chevilly-Larue"
	},
	"invoice" : [{
		"days" : 25,
		"dailyRate" : 450,
		"taxRate" : 20.0 // defaults to 20.0
	}, { ... }]
}

Response
--------

201 CREATED 
Content-type: application/pdf
Body : the generated binary pdf document
