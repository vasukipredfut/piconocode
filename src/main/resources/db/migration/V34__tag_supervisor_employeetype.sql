-- Tag the supervisors added in V9 so they can be queried on their own via employeetype.
UPDATE `employee_details` SET `employeetype` = 'Supervisor'
WHERE `employeename` IN (
  'Manoj R', 'Balakumar G', 'Amarovinappul M', 'Vijayakumar J', 'Praveen Kumar M',
  'Jeevanantham S', 'Deepan Babu M', 'Edwin Devaraj S', 'Elaventhan V', 'Kaviraj P R', 'Muthu M'
);
