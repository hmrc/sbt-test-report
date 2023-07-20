// Manually applied the following fix to enum.1.13.5.min.js for custom sorting of severity column:
// https://github.com/DataTables/Plugins/commit/d2b32a16eb72e67e48adc5780814d29e8fb123bb

DataTable.enum(['minor', 'moderate', 'serious', 'critical']);

new DataTable('#axe-violations', {
  order: [[4, "desc"]],
  pageLength: 100,
});
