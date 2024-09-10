function confirmDelete() {
    return confirm('Are you sure you want to delete this product?');
}

// Display success alert if set
document.addEventListener('DOMContentLoaded', function() {
var alertElement = document.getElementById('successAlert');
if (alertElement.textContent.trim() !== '') {
  alertElement.style.display = 'block';
  setTimeout(function() {
    alertElement.style.display = 'none';
  }, 5000);
}
});