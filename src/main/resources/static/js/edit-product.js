document.getElementById('editProductForm').addEventListener('submit', function(event) {
	  // Clear previous error messages
	  document.getElementById('nameError').style.display = 'none';
	  document.getElementById('priceError').style.display = 'none';

	  var isValid = true;

	  // Validate Product Name
	  var nameInput = document.getElementById('name').value.trim();
	  if (nameInput === '') {
		document.getElementById('nameError').style.display = 'block';
		isValid = false;
	  }

	  // Validate Product Price
	  var priceInput = document.getElementById('price').value;
	  if (priceInput < 0) {
		document.getElementById('priceError').style.display = 'block';
		isValid = false;
	  }

	  // Prevent form submission if validation fails
	  if (!isValid) {
		event.preventDefault();
	  }
});