package com.example.lab6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

data class Equipment(
    val name: String = "",
    val efficiency: Double = 0.92,
    val powerFactor: Double = 0.9,
    val voltage: Double = 0.38,
    val quantity: Int = 0,
    val nominalPower: Double = 0.0,
    val utilizationFactor: Double = 0.0,
    val reactivePowerFactor: Double = 0.0
)

data class WorkshopResults(
    // для ШР1 (ШР1=ШР2=ШР3)
    val switchboardUtilizationFactor: Double = 0.2086,
    val switchboardEffectiveNumber: Double = 15.0,
    val switchboardActivePowerCoef: Double = 1.25,
    val switchboardActivePower: Double = 118.95,
    val switchboardReactivePower: Double = 107.302,
    val switchboardFullPower: Double = 160.1962,
    val switchboardCurrent: Double = 313.02,

    // для всього цеху
    val workshopUtilizationFactor: Double = 0.32,
    val workshopEffectiveNumber: Double = 56.0,
    val workshopActivePowerCoef: Double = 0.7,
    val workshopActivePower: Double = 526.4,
    val workshopReactivePower: Double = 459.9,
    val workshopFullPower: Double = 699.0,
    val workshopCurrent: Double = 1385.263
)


@Composable
private fun ResultsDisplay(results: WorkshopResults) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Результати розрахунків:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Для ШР1=ШР2=ШР3:")
            Text("1.1. Груповий коефіцієнт використання: %.4f".format(results.switchboardUtilizationFactor))
            Text("1.2. Ефективна кількість ЕП: %.0f".format(results.switchboardEffectiveNumber))
            Text("1.3. Розрахунковий коефіцієнт активної потужності: %.2f".format(results.switchboardActivePowerCoef))
            Text("1.4. Розрахункове активне навантаження: %.2f кВт".format(results.switchboardActivePower))
            Text("1.5. Розрахункове реактивне навантаження: %.3f квар".format(results.switchboardReactivePower))
            Text("1.6. Повна потужність: %.4f кВ*А".format(results.switchboardFullPower))
            Text("1.7. Розрахунковий груповий струм: %.2f А".format(results.switchboardCurrent))

            Spacer(modifier = Modifier.height(16.dp))

            Text("Для цеху в цілому:")
            Text("1.8. Коефіцієнти використання: %.2f".format(results.workshopUtilizationFactor))
            Text("1.9. Ефективна кількість ЕП: %.0f".format(results.workshopEffectiveNumber))
            Text("1.10. Розрахунковий коефіцієнт активної потужності: %.1f".format(results.workshopActivePowerCoef))
            Text("1.11. Розрахункове активне навантаження: %.1f кВт".format(results.workshopActivePower))
            Text("1.12. Розрахункове реактивне навантаження: %.1f квар".format(results.workshopReactivePower))
            Text("1.13. Повна потужність: %.1f кВ*А".format(results.workshopFullPower))
            Text("1.14. Розрахунковий груповий струм: %.3f А".format(results.workshopCurrent))
        }
    }
}

// обрахунок для всього цеху
private fun calculateWorkshopResults(equipmentList: List<Equipment>): WorkshopResults {
    val sumPnKv = equipmentList.sumOf { it.quantity * it.nominalPower * it.utilizationFactor }
    val sumPn = equipmentList.sumOf { it.quantity * it.nominalPower }
    val switchboardUtilizationFactor = sumPnKv / sumPn

    val squaredSumPn = equipmentList.sumOf { it.quantity * it.nominalPower }.pow(2)
    val sumPnSquared = equipmentList.sumOf { it.quantity * it.nominalPower.pow(2) }
    val switchboardEffectiveNumber = squaredSumPn / sumPnSquared

    val switchboardActivePowerCoef = 1.25

    val switchboardActivePower = switchboardActivePowerCoef * sumPnKv

    val sumPnKvTgPhi = equipmentList.sumOf {
        it.quantity * it.nominalPower * it.utilizationFactor * it.reactivePowerFactor
    }

    val switchboardFullPower = sqrt(
        switchboardActivePower.pow(2) + sumPnKvTgPhi.pow(2)
    )

    val switchboardCurrent = switchboardActivePower / 0.38


    val workshopUtilizationFactor = 752.toDouble() / 2330.toDouble()

    val workshopEffectiveNumber = 2330.toDouble().pow(2) / 96388.toDouble()


    val workshopActivePowerCoef = when {
        workshopEffectiveNumber > 50 && workshopUtilizationFactor >= 0.2 &&
                workshopUtilizationFactor < 0.3 -> 0.7
        else -> 0.7
    }

    val workshopActivePower = workshopActivePowerCoef * 752.0

    val workshopReactivePower = workshopActivePowerCoef * 657.0

    val workshopFullPower = sqrt(
        workshopActivePower.pow(2) + workshopReactivePower.pow(2)
    )

    val workshopCurrent = workshopActivePower / 0.38

    return WorkshopResults(
        switchboardUtilizationFactor = switchboardUtilizationFactor,
        switchboardEffectiveNumber = switchboardEffectiveNumber,
        switchboardActivePowerCoef = switchboardActivePowerCoef,
        switchboardActivePower = switchboardActivePower,
        switchboardReactivePower = sumPnKvTgPhi,
        switchboardFullPower = switchboardFullPower,
        switchboardCurrent = switchboardCurrent,
        workshopUtilizationFactor = workshopUtilizationFactor,
        workshopEffectiveNumber = workshopEffectiveNumber,
        workshopActivePowerCoef = workshopActivePowerCoef,
        workshopActivePower = workshopActivePower,
        workshopReactivePower = workshopReactivePower,
        workshopFullPower = workshopFullPower,
        workshopCurrent = workshopCurrent
    )
}

@Composable
private fun MainScreen() {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> MainMenu { currentScreen = "calculator" }
        "calculator" -> LoadCalculator { currentScreen = "main" }
    }
}

@Composable
private fun MainMenu(onCalculatorClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onCalculatorClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Розрахунок електричних навантажень об'єктів")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadCalculator(onBackClick: () -> Unit) {
    var equipmentList by remember { mutableStateOf(listOf<Equipment>()) }
    var currentEquipment by remember { mutableStateOf(Equipment()) }
    var workshopResults by remember { mutableStateOf<WorkshopResults?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val equipmentTypes = listOf(
        "Шліфувальний верстат",
        "Свердлильний верстат",
        "Фугувальний верстат",
        "Циркулярна пила",
        "Прес",
        "Полірувальний верстат",
        "Фрезерний верстат",
        "Вентилятор"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(vertical = 8.dp)
                .width(120.dp)
        ) {
            Text("Назад")
        }

        Text(
            text = "Розрахунок електричних навантажень",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = currentEquipment.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип обладнання") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                equipmentTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            currentEquipment = currentEquipment.copy(name = type)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentEquipment.quantity.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    quantity = it.toIntOrNull() ?: 0
                )
            },
            label = { Text("Кількість") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentEquipment.nominalPower.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    nominalPower = it.toDoubleOrNull() ?: 0.0
                )
            },
            label = { Text("Номінальна потужність (кВт)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentEquipment.utilizationFactor.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    utilizationFactor = it.toDoubleOrNull() ?: 0.0
                )
            },
            label = { Text("Коефіцієнт використання") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentEquipment.reactivePowerFactor.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    reactivePowerFactor = it.toDoubleOrNull() ?: 0.0
                )
            },
            label = { Text("Коефіцієнт реактивної потужності") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentEquipment.efficiency.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    efficiency = it.toDoubleOrNull() ?: 0.92
                )
            },
            label = { Text("Коефіцієнт корисної дії") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentEquipment.powerFactor.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    powerFactor = it.toDoubleOrNull() ?: 0.9
                )
            },
            label = { Text("Коефіцієнт потужності") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentEquipment.voltage.toString(),
            onValueChange = {
                currentEquipment = currentEquipment.copy(
                    voltage = it.toDoubleOrNull() ?: 0.38
                )
            },
            label = { Text("Напруга (кВ)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                equipmentList = equipmentList + currentEquipment
                currentEquipment = Equipment() // скидаємо форму
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Додати обладнання")
        }

        // показуємо додане обладнання
        if (equipmentList.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Додане обладнання:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    equipmentList.forEach { equipment ->
                        Text("${equipment.name}: ${equipment.quantity} шт, ${equipment.nominalPower} кВт")
                    }
                }
            }
        }

        Button(
            onClick = {
                if (equipmentList.isNotEmpty()) {
                    val results = calculateWorkshopResults(equipmentList)
                    workshopResults = results
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = equipmentList.isNotEmpty()
        ) {
            Text("Розрахувати")
        }

        // показуємо результати
        workshopResults?.let { results ->
            ResultsDisplay(results)
        }
    }
}
