import 'package:reserve_bot_share_file/entity/citas_entity.dart';

class CitaModel extends Cita {
  CitaModel({
    required super.name,
    required super.hour,
    required super.date,
  });

  Map<String, String> toMap() {
    return {'nome': name, 'data': date, 'ora': hour};
  }

  factory CitaModel.fromJson(Map<String, dynamic> map) {
    return CitaModel(name: map["nome"], hour: map["data"], date: map["ora"]);
  }
}
