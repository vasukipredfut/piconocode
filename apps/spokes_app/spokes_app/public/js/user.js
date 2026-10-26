frappe.ui.form.on("User", {
    refresh: function (frm) {
        frm.add_custom_field({
            label: "Company",
            fieldname: "company",
            fieldtype: "Link",
            options: "Company"
        });
    }
});
