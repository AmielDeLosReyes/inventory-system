const inventoryData = {};
const productSelect = document.getElementById('productSelect');
const inventoryFormCard = document.getElementById('inventoryFormCard');
const inventoryTables = document.getElementById('inventoryTables');
const exportButton = document.getElementById('exportButton');
const costInput = document.getElementById('cost');
const currentBalanceInput = document.getElementById('currentBalance');
const currentQuantityInput = document.getElementById('currentQuantity');
const monthSelect = document.getElementById('monthSelect'); // New dropdown for month selection
let currentProduct = '';
let selectedMonth = '';

// Fetch and populate month options
function populateMonthOptions() {
    fetch('/inventory/months')
        .then(response => response.json())
        .then(months => {
            const monthSelect = document.getElementById('monthSelect');
            monthSelect.innerHTML = '<option value="">Select Month</option><option value="all">All Months</option>';
            months.forEach(month => {
                const option = document.createElement('option');
                option.value = month;
                option.textContent = new Date(month + '-01').toLocaleString('default', { month: 'long', year: 'numeric' });
                monthSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching months:', error));
}


// Call this function when the page loads
document.addEventListener('DOMContentLoaded', populateMonthOptions);


// Fetch products and populate the product selector
fetch('/products')
    .then(response => response.json())
    .then(products => {
        productSelect.innerHTML = '<option value="" disabled selected>Select Product</option>';
        products.forEach(product => {
            const option = document.createElement('option');
            option.value = product.name;
            option.textContent = product.name;
            productSelect.appendChild(option);
        });
    })
    .catch(error => console.error('Error fetching products:', error));

// Fetch the cost of the selected product
function fetchProductCost(productName) {
    fetch(`/products/${productName}/cost`)
        .then(response => response.json())
        .then(data => costInput.value = data || '')
        .catch(error => console.error('Error fetching product cost:', error));
}

// Fetch the latest balance for the selected product
function fetchLatestBalance(productName) {
    return fetch(`/inventory/latest-balance?productName=${encodeURIComponent(productName)}`)
        .then(response => response.json())
        .then(data => data.latestBalance || 0)
        .catch(error => console.error('Error fetching latest balance:', error));
}

// Fetch the latest quantity for the selected product
function fetchLatestQuantity(productName) {
    return fetch(`/inventory/latest-quantity?productName=${encodeURIComponent(productName)}`)
        .then(response => response.json())
        .then(data => data.latestQuantity || 0)
        .catch(error => console.error('Error fetching latest quantity:', error));
}

// Handle month selection
monthSelect.addEventListener('change', function() {
    selectedMonth = monthSelect.value;
    updateTables();
});



// Handle product selection
productSelect.addEventListener('change', function() {
    currentProduct = productSelect.value;
    document.getElementById('productName').value = currentProduct;
    inventoryFormCard.style.display = 'block';
    fetchProductCost(currentProduct);
    fetchLatestBalance(currentProduct).then(latestBalance => {
        currentBalanceInput.value = (latestBalance <= 0 || latestBalance === undefined) ? 'Out of Stock!' : latestBalance.toFixed(2);
        inventoryData[currentProduct] = inventoryData[currentProduct] || [];
        if (inventoryData[currentProduct].length > 0) {
            inventoryData[currentProduct][inventoryData[currentProduct].length - 1].balance = latestBalance;
        }
        updateTables();
    });

    // Fetch and update current quantity
    fetchLatestQuantity(currentProduct).then(latestQuantity => {
        currentQuantityInput.value = (latestQuantity <= 0 || latestQuantity === undefined) ? 'Out of Stock!' : latestQuantity;
    });
});



// Handle month selection
monthSelect.addEventListener('change', function() {
    selectedMonth = monthSelect.value;
    updateTables();
});

// Function to show an error message
function showErrorMessage(message) {
    const messageElement = document.createElement('div');
    messageElement.textContent = message;
    messageElement.className = 'text-danger'; // Add red text class
    inventoryFormCard.appendChild(messageElement);

    // Remove the message after 5 seconds
    setTimeout(() => {
        messageElement.remove();
    }, 5000); // 5000 milliseconds = 5 seconds
}

// Function to clear any error messages
function clearErrorMessage() {
    const existingMessage = inventoryFormCard.querySelector('.text-danger');
    if (existingMessage) {
        existingMessage.remove();
    }
}

// Handle inventory form submission
document.getElementById('inventoryForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const date = document.getElementById('entryDate').value; // Retrieve the date
    const description = document.getElementById('description').value;
    const cost = parseFloat(costInput.value);
    const quantity = parseInt(document.getElementById('quantity').value);

    let inAmount = 0;
    let outAmount = 0;
    let currentBalance = 0;
    let currentQuantity = 0; // Add this variable to track the quantity

    // Fetch the latest balance and quantity
    Promise.all([
        fetchLatestBalance(currentProduct),
        fetchLatestQuantity(currentProduct) // Fetch latest quantity
    ]).then(([latestBalance, latestQuantity]) => {
        currentBalance = latestBalance;
        currentQuantity = latestQuantity; // Set the current quantity

        // Handle purchase
        if (description === 'Purchase of Inventory' || description === 'Initial Inventory') {
            inAmount = cost * quantity;
            currentBalance += inAmount;
            currentQuantity += quantity; // Increase the quantity for purchases

        // Handle sale
        } else if (description === 'Sale of Merchandise') {
            outAmount = cost * quantity;

            if (quantity > currentQuantity) { // Check against currentQuantity
                showErrorMessage('You can\'t sell more than the available stock!');
                return; // Exit if not valid
            }
            currentBalance -= outAmount;
            currentQuantity -= quantity; // Decrease the quantity for sales

        // Handle damaged goods separately
        } else if (description === 'Damaged Goods') {
            outAmount = cost * quantity;

            if (quantity > currentQuantity) { // Check against currentQuantity
                showErrorMessage('You can\'t report damaged goods more than the available stock!');
                return; // Exit if not valid
            }
            currentBalance -= outAmount;
            currentQuantity -= quantity; // Decrease the quantity for damaged goods
        }

        // Entry object for saving
        const entry = {
            productName: currentProduct,
            entryDate: date,
            description: description,
            cost: cost,
            quantity: quantity,
            inAmount: inAmount,
            outAmount: outAmount,
            balance: currentBalance,
            latestQuantity: currentQuantity, // Include latestQuantity in the entry
            remarks: '' // Add any remarks if needed
        };

        if (!inventoryData[currentProduct]) {
            inventoryData[currentProduct] = [];
        }
        inventoryData[currentProduct].push(entry);

        // Save entry to server
        fetch('/inventory/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(entry)
        })
        .then(response => response.json())
        .then(data => {
            entry.id = data.id; // Update entry with ID
            Promise.all([
                fetchLatestBalance(currentProduct),
                fetchLatestQuantity(currentProduct) // Fetch the latest quantity again
            ]).then(([latestBalance, latestQuantity]) => {
                const balanceText = (latestBalance <= 0 || latestBalance === undefined) ? 'Out of Stock!' : latestBalance.toFixed(2);
                currentBalanceInput.value = balanceText;

                const quantityText = (latestQuantity <= 0 || latestQuantity === undefined) ? 'Out of Stock!' : latestQuantity;
                currentQuantityInput.value = quantityText; // Update the quantity input field

                clearErrorMessage(); // Clear any existing error messages
                updateTables(); // Update the UI tables
            });
        })
        .catch(error => console.error('Error saving entry:', error));
    });
});

// Handle update Tables ajax
// Filter entries by product and month, then update the tables
function updateTables() {
    if (!currentProduct) {
        return;
    }

    fetch(`/inventory/entries?productName=${encodeURIComponent(currentProduct)}&month=${encodeURIComponent(selectedMonth)}`)
        .then(response => response.json())
        .then(entries => {
            inventoryTables.innerHTML = ''; // Clear existing tables
            const groupedEntries = entries.reduce((acc, entry) => {
                const date = entry.entryDate.split('T')[0]; // Assuming entryDate is in ISO format
                if (!acc[date]) acc[date] = [];
                acc[date].push(entry);
                return acc;
            }, {});

            Object.keys(groupedEntries).forEach(date => {
                const table = document.createElement('table');
                table.className = 'table table-bordered table-striped';
                const thead = document.createElement('thead');
                thead.innerHTML = `
                    <tr>
                        <th>ID</th>
                        <th>Product Name</th>
                        <th>Date</th>
                        <th>Description</th>
                        <th>Cost</th>
                        <th>Quantity</th>
                        <th>In Amount</th>
                        <th>Out Amount</th>
                        <th>Balance</th>
                        <th>Remarks</th>
                        <th>Action</th> <!-- Add an Action column for the Remove button -->
                    </tr>
                `;
                table.appendChild(thead);

                const tbody = document.createElement('tbody');
                groupedEntries[date].forEach(entry => {
                    console.log('Entry ID:', entry.id); // Log the entry ID

                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${entry.id}</td>
                        <td>${entry.productName}</td>
                        <td>${entry.entryDate}</td>
                        <td>${entry.description}</td>
                        <td>${entry.cost}</td>
                        <td>${entry.quantity}</td>
                        <td>${entry.inAmount}</td>
                        <td>${entry.outAmount}</td>
                        <td>${entry.balance}</td>
                        <td>${entry.remarks || ''}</td> <!-- Default to empty string if remarks is null -->
                        <td><button class="btn btn-danger" onclick="deleteEntry(${entry.id})">Remove</button></td> <!-- Add Remove button -->
                    `;
                    tbody.appendChild(tr);
                });
                table.appendChild(tbody);
                inventoryTables.appendChild(table);
            });
        })
        .catch(error => console.error('Error fetching inventory entries:', error));
}


// Delete entry for delete button
function deleteEntry(entryId) {
    if (confirm('Are you sure you want to delete this entry?')) {
        fetch(`/inventory/delete/${entryId}`, {
            method: 'DELETE',
        })
        .then(response => {
            if (response.ok) {
                if (response.status === 204) {
                    // No content response, just resolve the promise
                    return Promise.resolve();
                }
                // Handle other successful responses if necessary
                return response.json();
            } else {
                // Handle HTTP errors
                return response.text().then(text => {
                    try {
                        const error = JSON.parse(text);
                        return Promise.reject(error);
                    } catch (e) {
                        return Promise.reject({ message: text });
                    }
                });
            }
        })
        .then(() => {
            // Fetch and update the current balance and quantity after successful deletion
            Promise.all([
                fetchLatestBalance(currentProduct),
                fetchLatestQuantity(currentProduct) // Fetch the latest quantity
            ])
            .then(([latestBalance, latestQuantity]) => {
                currentBalanceInput.value = (latestBalance <= 0 || latestBalance === undefined) ? 'Out of Stock!' : latestBalance.toFixed(2);

                // Update the quantity input field
                const quantityText = (latestQuantity <= 0 || latestQuantity === undefined) ? 'Out of Stock!' : latestQuantity;
                currentQuantityInput.value = quantityText;

                updateTables(); // Update the tables to reflect changes
                alert('Entry deleted successfully!');
            });
        })
        .catch(error => {
            console.error('Error deleting entry:', error);
            alert(`Failed to delete entry. ${error.message || 'Please try again.'}`);
        });
    }
}


// Handle export to Excel
exportButton.addEventListener('click', function() {
    window.location.href = '/document/export';
});