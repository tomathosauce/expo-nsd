import { FlatList, SafeAreaView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import * as ExpoNsd from 'expo-nsd';
import React, { useEffect, useState } from 'react';

const Item = ({ name, port, address }: ExpoNsd.Service) => (
  <View style={styles.item}>
    <Text style={{ fontSize: 30 }}>{name}</Text>
    <Text style={{ fontSize: 15 }}>{`${address}:${port}`}</Text>
  </View>
);

export default function App() {
  const [isPermissionGranted, setIsPermissionGranted] = useState(false);
  const [serviceList, setService] = useState<ExpoNsd.Service[]>([]);
  const [scanning, setScanning] = useState(false);

  useEffect(() => {
    const sub = ExpoNsd.configureDiscovery("_http._tcp", (service) => {
      console.debug(service)
      setService([...serviceList, service])
    })

    return () => sub.remove();
  }, []);

  const onPress = () => {
    if (isPermissionGranted) {
      if (scanning) {
        try {
          ExpoNsd.stopDiscovery()
        } catch (error) {

        }
        setScanning(false)
        setService([])
      } else {
        try {
          ExpoNsd.startDiscovery()
        } catch (error) {

        }
        setScanning(true)
      }
    } else {
      ExpoNsd.requestPermissions()
      setIsPermissionGranted(true)
    }
  }

  const text = !isPermissionGranted ? "Request Permissions" : (!scanning ? "Start Scan" : "Stop Scan")
  const buttonColor = !isPermissionGranted ? "turquoise" : (!scanning ? "blue" : "darkorange")

  return (
    <SafeAreaView style={styles.container}>
      {isPermissionGranted && <FlatList
        data={serviceList}
        renderItem={({ item }) => <Item {...item} />}
        keyExtractor={item => item.name}
        style={{marginBottom: 15}}
      />}
      <TouchableOpacity
        style={[styles.button, { backgroundColor: buttonColor }]}
        onPress={onPress}
      >
        <Text style={styles.buttonText}>
          {text}
        </Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: "center",
  },
  button: {
    height: 100,
    borderRadius: 20,
    justifyContent: "center",
    alignItems: "center",
  },
  buttonText: {
    color: "white",
    fontSize: 30,
  },
  item: {
    backgroundColor: 'lightblue',
    borderRadius: 20,
    padding: 20,
    marginVertical: 8,
  }
});